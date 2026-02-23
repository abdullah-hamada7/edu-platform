package com.securemath.integration.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.*;
import com.securemath.dto.quiz.QuizSubmissionRequestDto;
import com.securemath.dto.quiz.QuizSubmissionRequestDto.AnswerDto;
import com.securemath.repository.*;
import com.securemath.security.JwtTokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceIT {

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
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID courseId;
    private UUID lessonId;
    private UUID quizId;
    private UUID questionId;

    @BeforeEach
    void setUp() {
        Course course = Course.builder()
            .title("Performance Test Course")
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

        Quiz quiz = Quiz.builder()
            .courseId(lessonId)
            .title("Performance Quiz")
            
            .status(QuizStatus.PUBLISHED)
            .build();
        quizId = quizRepository.save(quiz).getId();

        Question question = Question.builder()
            .quizId(quizId)
            .promptText("What is 2 + 2?")
            .type(QuestionType.MCQ)
            .answerKey("{\"correct\": \"4\", \"options\": [\"3\", \"4\", \"5\", \"6\"]}")
            .points(BigDecimal.valueOf(10.0))
            .position(0)
            .build();
        questionId = questionRepository.save(question).getId();
    }

    private QuizSubmissionRequestDto createSubmission() {
        AnswerDto answer = AnswerDto.builder()
            .questionId(questionId)
            .response("4")
            .build();
        return QuizSubmissionRequestDto.builder()
            .answers(List.of(answer))
            .build();
    }

    @Test
    @Order(1)
    void quizGradingMeetsP95LatencyTarget() throws Exception {
        UserAccount student = UserAccount.builder()
            .email("perf-student@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        UUID studentId = userRepository.save(student).getId();
        String token = jwtTokenService.generateToken(studentId, "perf-student@test.com", "STUDENT");

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        List<Long> latencies = new ArrayList<>();
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            
            mockMvc.perform(post("/api/student/quiz/submit")
                    .header("Authorization", "Bearer " + token)
                    .header("X-Device-Fingerprint", "perf-device-" + i)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(createSubmission())))
                .andExpect(status().isOk());
            
            long latency = System.currentTimeMillis() - start;
            latencies.add(latency);
        }

        Collections.sort(latencies);
        long p95Latency = latencies.get((int) (iterations * 0.95));
        
        assertTrue(p95Latency < 300, 
            "P95 latency " + p95Latency + "ms exceeds 300ms target. Latencies: " + latencies);
    }

    @Test
    @Order(2)
    void playbackGrantRequestIsFast() throws Exception {
        UserAccount student = UserAccount.builder()
            .email("playback-perf@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        UUID studentId = userRepository.save(student).getId();
        String token = jwtTokenService.generateToken(studentId, "playback-perf@test.com", "STUDENT");

        Enrollment enrollment = Enrollment.builder()
            .studentId(studentId)
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        List<Long> latencies = new ArrayList<>();
        int iterations = 50;
        
        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            
            mockMvc.perform(post("/api/student/playback/grant")
                    .header("Authorization", "Bearer " + token)
                    .header("X-Device-Fingerprint", "playback-device")
                    .contentType("application/json")
                    .content("{\"lessonId\":\"" + lessonId + "\"}"))
                .andExpect(status().isOk());
            
            long latency = System.currentTimeMillis() - start;
            latencies.add(latency);
        }

        double avgLatency = latencies.stream().mapToLong(l -> l).average().orElse(0);
        
        assertTrue(avgLatency < 500, 
            "Average playback grant latency " + avgLatency + "ms exceeds 500ms target");
    }

    @Test
    @Order(3)
    void concurrentStudentAccessHandlesLoad() throws Exception {
        int concurrentStudents = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentStudents);
        CountDownLatch latch = new CountDownLatch(concurrentStudents);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentStudents; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                try {
                    UserAccount student = UserAccount.builder()
                        .email("concurrent-" + index + "@test.com")
                        .passwordHash(passwordEncoder.encode("password"))
                        .role(Role.STUDENT)
                        .status(AccountStatus.ACTIVE)
                        .build();
                    UUID studentId = userRepository.save(student).getId();
                    String token = jwtTokenService.generateToken(studentId, "concurrent-" + index + "@test.com", "STUDENT");

                    Enrollment enrollment = Enrollment.builder()
                        .studentId(studentId)
                        .courseId(courseId)
                        .status(EnrollmentStatus.ACTIVE)
                        .build();
                    enrollmentRepository.save(enrollment);

                    mockMvc.perform(get("/api/student/courses")
                            .header("Authorization", "Bearer " + token)
                            .header("X-Device-Fingerprint", "concurrent-device-" + index))
                        .andExpect(status().isOk());

                    mockMvc.perform(post("/api/student/playback/grant")
                            .header("Authorization", "Bearer " + token)
                            .header("X-Device-Fingerprint", "concurrent-device-" + index)
                            .contentType("application/json")
                            .content("{\"lessonId\":\"" + lessonId + "\"}"))
                        .andExpect(status().isOk());

                    mockMvc.perform(post("/api/student/quiz/submit")
                            .header("Authorization", "Bearer " + token)
                            .header("X-Device-Fingerprint", "concurrent-device-" + index)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(createSubmission())))
                        .andExpect(status().isOk());

                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    latch.countDown();
                }
            }));
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertTrue(completed, "Concurrent test timed out");

        executor.shutdown();

        long successCount = futures.stream()
            .filter(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    return false;
                }
            })
            .count();

        assertTrue(successCount >= concurrentStudents * 0.9, 
            "Only " + successCount + " of " + concurrentStudents + " concurrent requests succeeded");
    }

    @Test
    @Order(4)
    void courseListingPaginationPerformance() throws Exception {
        UserAccount student = UserAccount.builder()
            .email("pagination-perf@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        UUID studentId = userRepository.save(student).getId();
        String token = jwtTokenService.generateToken(studentId, "pagination-perf@test.com", "STUDENT");

        for (int i = 0; i < 20; i++) {
            Course extraCourse = Course.builder()
                .title("Extra Course " + i)
                .status(CourseStatus.PUBLISHED)
                .build();
            UUID extraCourseId = courseRepository.save(extraCourse).getId();
            
            Enrollment extraEnrollment = Enrollment.builder()
                .studentId(studentId)
                .courseId(extraCourseId)
                .status(EnrollmentStatus.ACTIVE)
                .build();
            enrollmentRepository.save(extraEnrollment);
        }

        long start = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/student/courses")
                .header("Authorization", "Bearer " + token)
                .header("X-Device-Fingerprint", "pagination-device"))
            .andExpect(status().isOk());
        
        long latency = System.currentTimeMillis() - start;

        assertTrue(latency < 1000, "Course listing took " + latency + "ms, exceeds 1s target");
    }
}
