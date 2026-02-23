package com.securemath.service;

import com.securemath.domain.*;
import com.securemath.dto.quiz.*;
import com.securemath.exception.EnrollmentRequiredException;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentQuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizGradingService gradingService;

    public QuizDetailDto getQuizForStudent(UUID quizId, UUID studentId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> ResourceNotFoundException.of("Quiz", quizId));

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw ResourceNotFoundException.of("Quiz", quizId);
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                studentId, quiz.getCourseId(), EnrollmentStatus.ACTIVE)) {
            throw new EnrollmentRequiredException("Not enrolled in this course");
        }

        return toDetailDto(quiz);
    }

    public List<QuizSummaryDto> listQuizzesForCourse(UUID courseId, UUID studentId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, EnrollmentStatus.ACTIVE)) {
            throw new EnrollmentRequiredException("Not enrolled in this course");
        }

        return quizRepository.findByCourseIdAndStatus(courseId, QuizStatus.PUBLISHED).stream()
            .map(quiz -> QuizSummaryDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .build())
            .collect(Collectors.toList());
    }

    @Transactional
    public QuizSubmissionResponseDto submitQuiz(UUID quizId, UUID studentId, QuizSubmissionRequestDto request) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> ResourceNotFoundException.of("Quiz", quizId));

        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw ResourceNotFoundException.of("Quiz", quizId);
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                studentId, quiz.getCourseId(), EnrollmentStatus.ACTIVE)) {
            throw new EnrollmentRequiredException("Not enrolled in this course");
        }

        if (attemptRepository.existsByQuizIdAndStudentId(quizId, studentId)) {
            throw new IllegalStateException("Quiz already submitted");
        }

        QuizGradingService.GradingResult result = gradingService.gradeSubmission(quizId, request.getAnswers());

        QuizAttempt attempt = QuizAttempt.builder()
            .quizId(quizId)
            .studentId(studentId)
            .submittedAt(Instant.now())
            .score(result.score())
            .maxScore(result.maxScore())
            .gradingLatencyMs(result.gradingLatencyMs())
            .build();

        QuizAttempt savedAttempt = attemptRepository.save(attempt);

        for (QuizGradingService.GradedAnswer ga : result.answers()) {
            Answer answer = Answer.builder()
                .attemptId(savedAttempt.getId())
                .questionId(ga.questionId())
                .responseValue(ga.responseValue())
                .isCorrect(ga.isCorrect())
                .awardedPoints(ga.awardedPoints())
                .build();
            answerRepository.save(answer);
        }

        return QuizSubmissionResponseDto.builder()
            .attemptId(savedAttempt.getId())
            .score(savedAttempt.getScore())
            .maxScore(savedAttempt.getMaxScore())
            .gradingLatencyMs(savedAttempt.getGradingLatencyMs())
            .build();
    }

    public List<GradeRecordDto> getStudentGrades(UUID studentId) {
        return attemptRepository.findByStudentIdOrderBySubmittedAtDesc(studentId).stream()
            .map(this::toGradeRecord)
            .collect(Collectors.toList());
    }

    public com.securemath.dto.student.StudentDashboardSummaryDto getDashboardSummary(UUID studentId) {
        List<Enrollment> activeEnrollments = enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        List<GradeRecordDto> grades = getStudentGrades(studentId);
        
        double avgScore = grades.stream()
            .mapToDouble(g -> g.getScore().doubleValue() / g.getMaxScore().doubleValue())
            .average()
            .orElse(0.0) * 100;

        // Calculate weekly progress (quizzes in last 7 days / goal of 5)
        long recentQuizzes = grades.stream()
            .filter(g -> g.getSubmittedAt().isAfter(java.time.Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS)))
            .count();
        int progress = (int) Math.min(100, (recentQuizzes * 100) / 5);

        // Calculate rank (rudimentary: if avg > 90 -> Top 5%, else if > 80 -> Top 20%, etc.)
        String rank = "Initiate";
        if (avgScore > 90) rank = "Top 5%";
        else if (avgScore > 80) rank = "Top 20%";
        else if (avgScore > 50) rank = "Professional";

        return com.securemath.dto.student.StudentDashboardSummaryDto.builder()
            .activeCoursesCount(activeEnrollments.size())
            .averageScore(avgScore)
            .platformRank(rank)
            .recentActivity(grades.stream().limit(5).collect(Collectors.toList()))
            .weeklyProgressPercentage(progress)
            .build();
    }

    private QuizDetailDto toDetailDto(Quiz quiz) {
        List<QuestionResponseDto> questions = questionRepository.findByQuizIdOrderByPosition(quiz.getId())
            .stream()
            .map(this::toQuestionDto)
            .collect(Collectors.toList());

        return QuizDetailDto.builder()
            .id(quiz.getId())
            .courseId(quiz.getCourseId())
            .title(quiz.getTitle())
            .timeLimitSeconds(quiz.getTimeLimitSeconds())
            .questions(questions)
            .build();
    }

    private QuestionResponseDto toQuestionDto(Question q) {
        return QuestionResponseDto.builder()
            .id(q.getId())
            .type(q.getType().name())
            .promptText(q.getPromptText())
            .latexEnabled(q.getLatexEnabled())
            .points(q.getPoints())
            .position(q.getPosition())
            .build();
    }

    private GradeRecordDto toGradeRecord(QuizAttempt attempt) {
        Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
        return GradeRecordDto.builder()
            .quizId(attempt.getQuizId())
            .quizTitle(quiz != null ? quiz.getTitle() : null)
            .score(attempt.getScore())
            .maxScore(attempt.getMaxScore())
            .submittedAt(attempt.getSubmittedAt())
            .gradingLatencyMs(attempt.getGradingLatencyMs())
            .build();
    }
}
