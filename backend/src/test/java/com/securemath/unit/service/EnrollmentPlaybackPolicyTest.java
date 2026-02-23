package com.securemath.unit.service;

import com.securemath.domain.*;
import com.securemath.exception.EnrollmentRequiredException;
import com.securemath.repository.*;
import com.securemath.video.SignedUrlService;
import com.securemath.video.WatermarkPolicyService;
import com.securemath.service.StudentPlaybackService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentPlaybackPolicyTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private PlaybackAccessGrantRepository grantRepository;

    @Mock
    private SignedUrlService signedUrlService;

    @Mock
    private WatermarkPolicyService watermarkPolicyService;

    private StudentPlaybackService playbackService;

    private UUID studentId;
    private UUID lessonId;
    private UUID courseId;
    private UUID chapterId;
    private UUID videoAssetId;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        chapterId = courseId;
        videoAssetId = UUID.randomUUID();
        
        playbackService = new StudentPlaybackService(
            enrollmentRepository,
            lessonRepository,
            grantRepository,
            signedUrlService,
            watermarkPolicyService
        );
        setField(playbackService, "expiryHours", 2);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void enrolledStudentCanGetPlaybackGrant() {
        Lesson lesson = Lesson.builder()
            .id(lessonId)
            .chapterId(chapterId)
            .title("Test Lesson")
            .videoAssetId(videoAssetId)
            .position(0)
            .build();

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(chapterId)
            .status(EnrollmentStatus.ACTIVE)
            .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndStatus(studentId, chapterId, EnrollmentStatus.ACTIVE))
            .thenReturn(Optional.of(enrollment));
        when(grantRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(signedUrlService.generateSignedUrl(eq(videoAssetId), eq(studentId)))
            .thenReturn("https://example.com/signed-url");
        when(watermarkPolicyService.generateWatermarkSeed(eq(studentId), eq(lessonId)))
            .thenReturn("watermark-seed");

        var result = playbackService.requestPlaybackGrant(studentId, lessonId, "fingerprint");

        assertNotNull(result);
        assertEquals("https://example.com/signed-url", result.getManifestUrl());
        assertEquals("watermark-seed", result.getWatermarkSeed());
        assertNotNull(result.getExpiresAt());
    }

    @Test
    void unenrolledStudentCannotGetPlaybackGrant() {
        Lesson lesson = Lesson.builder()
            .id(lessonId)
            .chapterId(chapterId)
            .title("Test Lesson")
            .videoAssetId(videoAssetId)
            .position(0)
            .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndStatus(studentId, chapterId, EnrollmentStatus.ACTIVE))
            .thenReturn(Optional.empty());

        assertThrows(EnrollmentRequiredException.class, () -> 
            playbackService.requestPlaybackGrant(studentId, lessonId, "fingerprint"));
    }

    @Test
    void removedEnrollmentCannotGetPlaybackGrant() {
        Lesson lesson = Lesson.builder()
            .id(lessonId)
            .chapterId(chapterId)
            .title("Test Lesson")
            .videoAssetId(videoAssetId)
            .position(0)
            .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndStatus(studentId, chapterId, EnrollmentStatus.ACTIVE))
            .thenReturn(Optional.empty());

        assertThrows(EnrollmentRequiredException.class, () -> 
            playbackService.requestPlaybackGrant(studentId, lessonId, "fingerprint"));
    }

    @Test
    void lessonWithoutVideoCannotGetPlaybackGrant() {
        Lesson lesson = Lesson.builder()
            .id(lessonId)
            .chapterId(chapterId)
            .title("Test Lesson")
            .videoAssetId(null)
            .position(0)
            .build();

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(chapterId)
            .status(EnrollmentStatus.ACTIVE)
            .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndStatus(studentId, chapterId, EnrollmentStatus.ACTIVE))
            .thenReturn(Optional.of(enrollment));

        assertThrows(IllegalStateException.class, () -> 
            playbackService.requestPlaybackGrant(studentId, lessonId, "fingerprint"));
    }

    @Test
    void playbackGrantHasCorrectExpiry() {
        Lesson lesson = Lesson.builder()
            .id(lessonId)
            .chapterId(chapterId)
            .title("Test Lesson")
            .videoAssetId(videoAssetId)
            .position(0)
            .build();

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(chapterId)
            .status(EnrollmentStatus.ACTIVE)
            .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndStatus(studentId, chapterId, EnrollmentStatus.ACTIVE))
            .thenReturn(Optional.of(enrollment));
        when(grantRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(signedUrlService.generateSignedUrl(any(), any())).thenReturn("url");
        when(watermarkPolicyService.generateWatermarkSeed(any(), any())).thenReturn("seed");

        var result = playbackService.requestPlaybackGrant(studentId, lessonId, "fingerprint");

        assertTrue(result.getExpiresAt().isAfter(Instant.now()));
        assertTrue(result.getExpiresAt().isBefore(Instant.now().plusSeconds(3 * 60 * 60)));
    }
}
