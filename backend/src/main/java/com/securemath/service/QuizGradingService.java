package com.securemath.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.securemath.domain.*;
import com.securemath.dto.quiz.*;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizGradingService {

    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public GradingResult gradeSubmission(UUID quizId, List<QuizSubmissionRequestDto.AnswerDto> answers) {
        long startTime = System.currentTimeMillis();
        
        List<Question> questions = questionRepository.findByQuizIdOrderByPosition(quizId);
        Map<UUID, Question> questionMap = new HashMap<>();
        questions.forEach(q -> questionMap.put(q.getId(), q));

        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;
        List<GradedAnswer> gradedAnswers = new ArrayList<>();

        for (QuizSubmissionRequestDto.AnswerDto answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                continue;
            }

            maxScore = maxScore.add(question.getPoints());
            boolean isCorrect = evaluateAnswer(question, answer.getResponse());
            BigDecimal awardedPoints = isCorrect ? question.getPoints() : BigDecimal.ZERO;
            totalScore = totalScore.add(awardedPoints);

            gradedAnswers.add(new GradedAnswer(
                question.getId(),
                answer.getResponse(),
                isCorrect,
                awardedPoints
            ));
        }

        long latency = System.currentTimeMillis() - startTime;

        return new GradingResult(totalScore, maxScore, (int) latency, gradedAnswers);
    }

    private boolean evaluateAnswer(Question question, String response) {
        try {
            JsonNode answerKey = objectMapper.readTree(question.getAnswerKey());

            return switch (question.getType()) {
                case MCQ -> evaluateMcq(answerKey, response);
                case TRUE_FALSE -> evaluateTrueFalse(answerKey, response);
                case NUMERIC -> evaluateNumeric(answerKey, response);
            };
        } catch (Exception e) {
            log.error("Error evaluating answer for question {}", question.getId(), e);
            return false;
        }
    }

    private boolean evaluateMcq(JsonNode answerKey, String response) {
        int correctIndex = answerKey.path("correctIndex").asInt(-1);
        try {
            int selectedIndex = Integer.parseInt(response);
            return selectedIndex == correctIndex;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean evaluateTrueFalse(JsonNode answerKey, String response) {
        boolean correctAnswer = answerKey.path("value").asBoolean();
        return Boolean.parseBoolean(response) == correctAnswer;
    }

    private boolean evaluateNumeric(JsonNode answerKey, String response) {
        try {
            double expected = answerKey.path("value").asDouble();
            double tolerance = answerKey.path("tolerance").asDouble(0.01);
            double actual = Double.parseDouble(response);
            return Math.abs(actual - expected) <= tolerance;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public record GradingResult(
        BigDecimal score,
        BigDecimal maxScore,
        int gradingLatencyMs,
        List<GradedAnswer> answers
    ) {}

    public record GradedAnswer(
        UUID questionId,
        String responseValue,
        boolean isCorrect,
        BigDecimal awardedPoints
    ) {}
}

