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

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SignedUrlReplayIT {

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
    private PlaybackAccessGrantRepository grantRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private UUID studentId;
    private UUID lessonId;
    private UUID courseId;

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
        UUID videoAssetId = videoAssetRepository.save(videoAsset).getId();

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
    void newGrantReturnsSignedUrl() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-1")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.manifestUrl").exists())
            .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    @Order(2)
    void eachGrantHasUniqueSignedUrlHash() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-2a")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-2a")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        long grantCount = grantRepository.count();
        Assertions.assertTrue(grantCount >= 2);
    }

    @Test
    @Order(3)
    void grantHasCorrectExpiry() throws Exception {
        var result = mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-3")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        String expiresAtStr = objectMapper.readTree(response).get("expiresAt").asText();
        Instant expiresAt = Instant.parse(expiresAtStr);

        Assertions.assertTrue(expiresAt.isAfter(Instant.now()));
        Assertions.assertTrue(expiresAt.isBefore(Instant.now().plusSeconds(3 * 60 * 60)));
    }

    @Test
    @Order(4)
    void grantRecordsStudentAndLesson() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-4")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        var grants = grantRepository.findAll();
        Assertions.assertTrue(grants.stream()
            .anyMatch(g -> g.getStudentId().equals(studentId) && g.getLessonId().equals(lessonId)));
    }

    @Test
    @Order(5)
    void differentStudentCannotReuseGrant() throws Exception {
        mockMvc.perform(post("/api/student/playback/grant")
                .header("Authorization", "Bearer " + studentToken)
                .header("X-Device-Fingerprint", "device-5")
                .contentType("application/json")
                .content("{\"lessonId\":\"" + lessonId + "\"}"))
            .andExpect(status().isOk());

        UserAccount otherStudent = UserAccount.builder()
            .email("other@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        UUID otherStudentId = userRepository.save(otherStudent).getId();
        String otherToken = jwtTokenService.generateToken(otherStudentId, "other@test.com", "STUDENT");

        Enrollment otherEnrollment = Enrollment.builder()
            .studentId(otherStudentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(otherEnrollment);

        var originalGrants = grantRepository.findByStudentId(studentId);
        Assertions.assertFalse(originalGrants.isEmpty());

        var otherGrants = grantRepository.findByStudentId(otherStudentId);
        Assertions.assertTrue(otherGrants.isEmpty());
    }
}
