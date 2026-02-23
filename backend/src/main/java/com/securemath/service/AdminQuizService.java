package com.securemath.service;

import com.securemath.domain.Question;
import com.securemath.domain.QuestionType;
import com.securemath.domain.Quiz;
import com.securemath.domain.QuizStatus;
import com.securemath.dto.admin.AdminQuestionCreateDto;
import com.securemath.dto.admin.AdminQuizCreateDto;
import com.securemath.dto.quiz.QuizSummaryDto;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.CourseRepository;
import com.securemath.repository.QuestionRepository;
import com.securemath.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminQuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public QuizSummaryDto createQuiz(UUID courseId, AdminQuizCreateDto dto) {
        courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));

        QuizStatus status = dto.getStatus() != null ? QuizStatus.valueOf(dto.getStatus()) : QuizStatus.DRAFT;

        Quiz quiz = Quiz.builder()
            .courseId(courseId)
            .title(dto.getTitle())
            .timeLimitSeconds(dto.getTimeLimitSeconds())
            .status(status)
            .build();

        return toSummaryDto(quizRepository.save(quiz));
    }

    public List<QuizSummaryDto> listQuizzes(UUID courseId) {
        return quizRepository.findByCourseId(courseId).stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
    }

    public List<QuizSummaryDto> listAllQuizzes() {
        return quizRepository.findAll().stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void publishQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> ResourceNotFoundException.of("Quiz", quizId));
        quiz.setStatus(QuizStatus.PUBLISHED);
        quizRepository.save(quiz);
    }

    @Transactional
    public Question addQuestion(UUID quizId, AdminQuestionCreateDto dto) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> ResourceNotFoundException.of("Quiz", quizId));

        int position = dto.getPosition() != null ? dto.getPosition() : (int) questionRepository.countByQuizId(quizId);

        Question question = Question.builder()
            .quizId(quiz.getId())
            .type(QuestionType.valueOf(dto.getType()))
            .promptText(dto.getPromptText())
            .latexEnabled(dto.getLatexEnabled() != null ? dto.getLatexEnabled() : false)
            .answerKey(dto.getAnswerKey())
            .points(dto.getPoints())
            .position(position)
            .build();

        return questionRepository.save(question);
    }

    private QuizSummaryDto toSummaryDto(Quiz quiz) {
        return QuizSummaryDto.builder()
            .id(quiz.getId())
            .title(quiz.getTitle())
            .status(quiz.getStatus().name())
            .build();
    }
}
