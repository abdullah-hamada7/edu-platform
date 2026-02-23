package com.securemath.unit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.Question;
import com.securemath.domain.QuestionType;
import com.securemath.dto.quiz.QuizSubmissionRequestDto;
import com.securemath.repository.QuestionRepository;
import com.securemath.service.QuizGradingService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizGradingServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    private QuizGradingService gradingService;
    private ObjectMapper objectMapper;

    private UUID quizId;
    private Question mcqQuestion;
    private Question trueFalseQuestion;
    private Question numericQuestion;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        gradingService = new QuizGradingService(questionRepository, objectMapper);
        
        quizId = UUID.randomUUID();

        mcqQuestion = Question.builder()
            .id(UUID.randomUUID())
            .quizId(quizId)
            .type(QuestionType.MCQ)
            .promptText("What is 2 + 2?")
            .answerKey("{\"correctIndex\":2,\"options\":[\"3\",\"4\",\"5\",\"6\"]}")
            .points(BigDecimal.valueOf(10.0))
            .position(0)
            .build();

        trueFalseQuestion = Question.builder()
            .id(UUID.randomUUID())
            .quizId(quizId)
            .type(QuestionType.TRUE_FALSE)
            .promptText("The earth is flat")
            .answerKey("{\"value\":false}")
            .points(BigDecimal.valueOf(5.0))
            .position(1)
            .build();

        numericQuestion = Question.builder()
            .id(UUID.randomUUID())
            .quizId(quizId)
            .type(QuestionType.NUMERIC)
            .promptText("What is pi to 2 decimal places?")
            .answerKey("{\"value\":3.14,\"tolerance\":0.01}")
            .points(BigDecimal.valueOf(15.0))
            .position(2)
            .build();

        when(questionRepository.findByQuizIdOrderByPosition(quizId))
            .thenReturn(List.of(mcqQuestion, trueFalseQuestion, numericQuestion));
    }

    @Test
    void gradeAllCorrectAnswers() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(mcqQuestion.getId(), "2"),
            new QuizSubmissionRequestDto.AnswerDto(trueFalseQuestion.getId(), "false"),
            new QuizSubmissionRequestDto.AnswerDto(numericQuestion.getId(), "3.14")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertEquals(0, BigDecimal.valueOf(30.0).compareTo(result.score()));
        assertEquals(0, BigDecimal.valueOf(30.0).compareTo(result.maxScore()));
        assertTrue(result.gradingLatencyMs() >= 0);
        assertEquals(3, result.answers().size());
        
        assertTrue(result.answers().get(0).isCorrect());
        assertTrue(result.answers().get(1).isCorrect());
        assertTrue(result.answers().get(2).isCorrect());
    }

    @Test
    void gradePartialCorrectAnswers() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(mcqQuestion.getId(), "0"),
            new QuizSubmissionRequestDto.AnswerDto(trueFalseQuestion.getId(), "false"),
            new QuizSubmissionRequestDto.AnswerDto(numericQuestion.getId(), "3.14")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertEquals(0, BigDecimal.valueOf(20.0).compareTo(result.score()));
        assertEquals(0, BigDecimal.valueOf(30.0).compareTo(result.maxScore()));
        
        assertFalse(result.answers().get(0).isCorrect());
        assertTrue(result.answers().get(1).isCorrect());
        assertTrue(result.answers().get(2).isCorrect());
    }

    @Test
    void gradeNumericWithTolerance() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(numericQuestion.getId(), "3.145")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertTrue(result.answers().get(0).isCorrect());
    }

    @Test
    void gradeNumericOutsideTolerance() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(numericQuestion.getId(), "3.20")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertFalse(result.answers().get(0).isCorrect());
    }

    @Test
    void gradeTrueFalseCorrect() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(trueFalseQuestion.getId(), "false")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertTrue(result.answers().get(0).isCorrect());
    }

    @Test
    void gradeTrueFalseIncorrect() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(trueFalseQuestion.getId(), "true")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertFalse(result.answers().get(0).isCorrect());
    }

    @Test
    void gradingLatencyIsRecorded() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(mcqQuestion.getId(), "2")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertTrue(result.gradingLatencyMs() < 300, "Grading should complete in under 300ms");
    }

    @Test
    void invalidAnswerFormatReturnsIncorrect() {
        List<QuizSubmissionRequestDto.AnswerDto> answers = List.of(
            new QuizSubmissionRequestDto.AnswerDto(mcqQuestion.getId(), "invalid")
        );

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, answers);

        assertFalse(result.answers().get(0).isCorrect());
    }
}
