package com.securemath.service;

import com.securemath.domain.*;
import com.securemath.dto.student.*;
import com.securemath.exception.EnrollmentRequiredException;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.*;
import com.securemath.video.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentPlaybackService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final PlaybackAccessGrantRepository grantRepository;
    private final SignedUrlService signedUrlService;
    private final WatermarkPolicyService watermarkPolicyService;

    @Value("${app.signed-url-expiry-hours:2}")
    private int expiryHours;

    @Transactional
    public PlaybackGrantResponseDto requestPlaybackGrant(UUID studentId, UUID lessonId, String deviceFingerprint) {
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> ResourceNotFoundException.of("Lesson", lessonId));

        UUID courseId = getCourseIdForLesson(lesson);
        
        Enrollment enrollment = enrollmentRepository
            .findByStudentIdAndCourseIdAndStatus(studentId, courseId, EnrollmentStatus.ACTIVE)
            .orElseThrow(() -> new EnrollmentRequiredException("Not enrolled in this course"));

        if (lesson.getVideoAssetId() == null) {
            throw new IllegalStateException("Lesson has no video content");
        }

        PlaybackAccessGrant grant = PlaybackAccessGrant.builder()
            .studentId(studentId)
            .lessonId(lessonId)
            .expiresAt(Instant.now().plusSeconds(expiryHours * 3600L))
            .signedUrlHash(UUID.randomUUID().toString())
            .build();
        
        grant = grantRepository.save(grant);

        String manifestUrl = signedUrlService.generateSignedUrl(lesson.getVideoAssetId(), studentId);
        String watermarkSeed = watermarkPolicyService.generateWatermarkSeed(studentId, lessonId);

        return PlaybackGrantResponseDto.builder()
            .manifestUrl(manifestUrl)
            .expiresAt(grant.getExpiresAt())
            .watermarkSeed(watermarkSeed)
            .build();
    }

    private UUID getCourseIdForLesson(Lesson lesson) {
        return lessonRepository.findById(lesson.getId())
            .map(l -> {
                if (l.getChapterId() == null) return null;
                return l.getChapterId();
            })
            .orElse(null);
    }
}
