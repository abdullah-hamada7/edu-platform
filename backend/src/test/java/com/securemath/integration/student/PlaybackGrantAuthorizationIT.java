package com.securemath.integration.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.*;
import com.securemath.repository.*;
import com.securemath.security.JwtTokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaybackGrantAuthorizationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private VideoAssetRepository videoAssetRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private String otherStudentToken;
    private UUID studentId;
    private UUID otherStudentId;
    private UUID lessonId;
    private UUID courseId;
    private UUID videoAssetId;

    @BeforeEach
    void setUp() {
        studentId = createUser("student@test.com", Role.STUDENT);
        studentToken = generateToken(studentId, "student@test.com", "STUDENT");

        otherStudentId = createUser("other@test.com", Role.STUDENT);
        otherStudentToken = generateToken(otherStudentId, "other@test.com", "STUDENT");

        Course course = Course.builder()
            .title("Test Course")
            .status(CourseStatus.PUBLISHED)
            .build();
        courseId = courseRepository.save(course).getId();

        Chapter chapter = Chapter.builder()
            .courseId(courseId)
            .title("Chapter 1")
            .position(0)
            .build();
        UUID chapterId = chapterRepository.save(chapter).getId();

        VideoAsset videoAsset = VideoAsset.builder()
            .rawObjectKey("test.mp4")
            .rawObjectKey("videos/test.mp4")
            .transcodeStatus(TranscodeStatus.READY)
            .build();
        videoAssetId = videoAssetRepository.save(videoAsset).getId();

        Lesson lesson = Lesson.builder()
            .chapterId(chapterId)
            .title("Lesson 1")
            .position(0)
            .videoAssetId(videoAssetId)
            .build();
        lessonId = lessonRepository.save(lesson).getId();
    }

    private UUID createUser(String email, Role role) {
        UserAccount user = UserAccount.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode("password"))
            .role(role)
            .status(AccountStatus.ACTIVE)
            .build();
        return userRepository.save(user).getId();
    }

    private String generateToken(UUID userId, String email, String role) {
        return jwtTokenService.generateToken(userId, email, role);
    }

    @Test
    @Order(1)
    void enrolledStudentCanRequestPlaybackGrant() throws Exception {
        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-fingerprint-1")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.manifestUrl").exists())
            .andExpect(jsonPath("$.expiresAt").exists())
            .andExpect(jsonPath("$.watermarkSeed").exists());
    }

    @Test
    @Order(2)
    void unenrolledStudentCannotRequestPlaybackGrant() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + otherStudentToken)
                .header("X-Device-Fingerprint", "device-fingerprint-2")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void removedEnrollmentCannotRequestPlaybackGrant() throws Exception {
        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.REMOVED)
            .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-fingerprint-3")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    void removedEnrollmentCannotAccessOtherCourseContent() throws Exception {
        Course otherCourse = Course.builder()
            .title("Other Course")
            .status(CourseStatus.PUBLISHED)
            .build();
        UUID otherCourseId = courseRepository.save(otherCourse).getId();

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(otherCourseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-fingerprint-4")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    void playbackGrantRequiresValidLesson() throws Exception {
        UUID invalidLessonId = UUID.randomUUID();

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-fingerprint-5")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + invalidLessonId + "\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    void playbackGrantRequiresDeviceFingerprint() throws Exception {
        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void lessonWithoutVideoReturnsError() throws Exception {
        Lesson noVideoLesson = Lesson.builder()
            .chapterId(lessonRepository.findById(lessonId).get().getChapterId())
            .title("No Video Lesson")
            .position(1)
            .videoAssetId(null)
            .build();
        UUID noVideoLessonId = lessonRepository.save(noVideoLesson).getId();

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-fingerprint-6")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + noVideoLessonId + "\"}"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(8)
    void inactiveUserCannotRequestPlaybackGrant() throws Exception {
        UserAccount inactive = UserAccount.builder()
            .email("inactive@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.INACTIVE)
            .build();
        UUID inactiveId = userRepository.save(inactive).getId();
        String inactiveToken = jwtTokenService.generateToken(inactiveId, "inactive@test.com", "STUDENT");

        Enrollment enrollment = Enrollment.builder()
            .studentId(inactiveId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + inactiveToken)
                .header("X-Device-Fingerprint", "device-fingerprint-7")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isForbidden());
    }
}
