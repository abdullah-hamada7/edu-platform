package com.securemath.integration.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.*;
import com.securemath.dto.quiz.QuizSubmissionRequestDto;
import com.securemath.repository.*;
import com.securemath.security.JwtTokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuizSubmissionIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private UUID quizId;
    private UUID questionId;

    @BeforeEach
    void setUp() throws Exception {
        UserAccount student = UserAccount.builder()
            .email("student@test.com")
            .passwordHash(passwordEncoder.encode("password"))
            .role(Role.STUDENT)
            .status(AccountStatus.ACTIVE)
            .build();
        student = userRepository.save(student);
        studentToken = jwtTokenService.generateToken(student.getId(), student.getEmail(), student.getRole().name());

        Course course = Course.builder()
            .title("Math Course")
            .status(CourseStatus.PUBLISHED)
            .build();
        UUID courseId = courseRepository.save(course).getId();

        Enrollment enrollment = Enrollment.builder()
            .studentId(student.getId())
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        enrollmentRepository.save(enrollment);

        Quiz quiz = Quiz.builder()
            .courseId(courseId)
            .title("Basic Math Quiz")
            .status(QuizStatus.PUBLISHED)
            .build();
        quizId = quizRepository.save(quiz).getId();

        Question question = Question.builder()
            .quizId(quizId)
            .type(QuestionType.MCQ)
            .promptText("What is 2 + 2?")
            .answerKey("{\"correctIndex\":1,\"options\":[\"3\",\"4\",\"5\"]}")
            .points(BigDecimal.valueOf(10.0))
            .position(0)
            .build();
        questionId = questionRepository.save(question).getId();
    }

    @Test
    @Order(1)
    void studentCanGetQuiz() throws Exception {
        mockMvc.perform(get("/api/student/quizzes/" + quizId)
                .header("Authorization", "Bearer " + studentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Basic Math Quiz"))
            .andExpect(jsonPath("$.questions", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    @Order(2)
    void studentCanSubmitQuiz() throws Exception {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(questionId, "1")
        );

        QuizSubmissionRequestDto request = QuizSubmissionRequestDto.builder()
            .answers(answers)
            .build();

        mockMvc.perform(post("/api/student/quizzes/" + quizId + "/submit")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(10.0))
            .andExpect(jsonPath("$.maxScore").value(10.0))
            .andExpect(jsonPath("$.gradingLatencyMs").isNumber())
            .andExpect(jsonPath("$.attemptId").exists());
    }

    @Test
    @Order(3)
    void studentCannotSubmitQuizTwice() throws Exception {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(questionId, "1")
        );

        QuizSubmissionRequestDto request = QuizSubmissionRequestDto.builder()
            .answers(answers)
            .build();

        mockMvc.perform(post("/api/student/quizzes/" + quizId + "/submit")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/student/quizzes/" + quizId + "/submit")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void gradingPersistsAttempt() throws Exception {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(questionId, "1")
        );

        QuizSubmissionRequestDto request = QuizSubmissionRequestDto.builder()
            .answers(answers)
            .build();

        String response = mockMvc.perform(post("/api/student/quizzes/" + quizId + "/submit")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String attemptId = objectMapper.readTree(response).get("attemptId").asText();
        assert attemptRepository.findById(UUID.fromString(attemptId)).isPresent();
    }

    @Test
    @Order(5)
    void gradingLatencyUnder300ms() throws Exception {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(questionId, "1")
        );

        QuizSubmissionRequestDto request = QuizSubmissionRequestDto.builder()
            .answers(answers)
            .build();

        String response = mockMvc.perform(post("/api/student/quizzes/" + quizId + "/submit")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        int latency = objectMapper.readTree(response).get("gradingLatencyMs").asInt();
        assertTrue(latency < 300, "Grading latency should be under 300ms, was: " + latency + "ms");
    }
}
