package com.securemath.integration.security;

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
class DeviceLimitIT {

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
    private RegisteredDeviceRepository deviceRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private UUID studentId;
    private UUID lessonId;
    private UUID courseId;
    private UUID videoAssetId;

    @BeforeEach
    void setUp() {
        UserAccount student = UserAccount.builder()
            .email("student@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        studentId = userRepository.save(student).getId();
        studentToken = jwtTokenService.generateToken(studentId, "student@test.com", "STUDENT");

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

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);
    }

    @Test
    @Order(1)
    void firstDeviceIsRegistered() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-1")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        long count = deviceRepository.countByStudentId(studentId);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(2)
    void secondDeviceIsRegistered() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-2a")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-2b")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        long count = deviceRepository.countByStudentId(studentId);
        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(3)
    void thirdDeviceIsRejected() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-3a")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-3b")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-3c")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Maximum device limit reached (2 devices)"));

        long count = deviceRepository.countByStudentId(studentId);
        Assertions.assertEquals(2, count);
    }

    @Test
    @Order(4)
    void alreadyRegisteredDeviceCanAccess() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-4a")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-4b")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-4a")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void differentStudentsHaveSeparateDeviceLimits() throws Exception {
        UserAccount student2 = UserAccount.builder()
            .email("student2@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        UUID student2Id = userRepository.save(student2).getId();
        String student2Token = jwtTokenService.generateToken(student2Id, "student2@test.com", "STUDENT");

        Enrollment enrollment2 = Enrollment.builder()
            .studentId(student2Id)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment2);

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-5a")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-5b")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + student2Token)
                .header("X-Device-Fingerprint", "device-5c")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + student2Token)
                .header("X-Device-Fingerprint", "device-5d")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void adminBypassesDeviceLimit() throws Exception {
        UserAccount admin = UserAccount.builder()
            .email("admin@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.ADMIN)
            .status(AccountStatus.ACTIVE)
            .build();
        UUID adminId = userRepository.save(admin).getId();
        String adminToken = jwtTokenService.generateToken(adminId, "admin@test.com", "ADMIN");

        mockMvc.perform(get("/api/admin/courses")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());

        long count = deviceRepository.countByStudentId(adminId);
        Assertions.assertEquals(0, count);
    }

    @Test
    @Order(7)
    void sameDeviceSameFingerprint() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "same-device-fingerprint")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "same-device-fingerprint")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        long count = deviceRepository.countByStudentId(studentId);
        Assertions.assertEquals(1, count);
    }
}
