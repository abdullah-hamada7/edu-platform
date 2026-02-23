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
    private final ChapterRepository chapterRepository;
    private final VideoAssetRepository videoAssetRepository;
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

        VideoAsset asset = videoAssetRepository.findById(lesson.getVideoAssetId())
            .orElseThrow(() -> ResourceNotFoundException.of("VideoAsset", lesson.getVideoAssetId()));

        if (asset.getTranscodeStatus() != TranscodeStatus.READY) {
            throw new IllegalStateException("Video is not ready for playback yet");
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
            .courseId(courseId)
            .build();
    }

    private UUID getCourseIdForLesson(Lesson lesson) {
        if (lesson.getChapterId() == null) {
            throw new IllegalStateException("Lesson is missing chapter association");
        }

        Chapter chapter = chapterRepository.findById(lesson.getChapterId())
            .orElseThrow(() -> ResourceNotFoundException.of("Chapter", lesson.getChapterId()));

        return chapter.getCourseId();
    }
}
