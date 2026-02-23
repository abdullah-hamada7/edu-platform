package com.securemath.service;

import com.securemath.domain.*;
import com.securemath.dto.admin.AdminDashboardSummaryDto;
import com.securemath.dto.quiz.GradeRecordDto;
import com.securemath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final UserAccountRepository userAccountRepository;
    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public AdminDashboardSummaryDto getDashboardSummary() {
        long totalStudents = userAccountRepository.countByRoleAndStatus(Role.STUDENT, AccountStatus.ACTIVE);
        long activeCourses = courseRepository.countByStatus(CourseStatus.PUBLISHED);
        long totalQuizzes = quizRepository.count();

        List<QuizAttempt> allAttempts = quizAttemptRepository.findAll();
        double avgPerformance = allAttempts.stream()
            .mapToDouble(a -> (double) a.getScore() / a.getMaxScore())
            .average()
            .orElse(0.0) * 100;

        return AdminDashboardSummaryDto.builder()
            .totalStudents(totalStudents)
            .activeCourses(activeCourses)
            .totalExaminations(totalQuizzes)
            .systemStatus("HEALTHY")
            .averagePerformance(avgPerformance)
            .storageUtilization(45) // Placeholder
            .build();
    }
}
