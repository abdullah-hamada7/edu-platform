package com.securemath.api.admin;

import com.securemath.domain.QuizAttempt;
import com.securemath.dto.quiz.GradeRecordDto;
import com.securemath.repository.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final QuizAttemptRepository quizAttemptRepository;
    private final com.securemath.service.AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/summary")
    public ResponseEntity<com.securemath.dto.admin.AdminDashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(adminAnalyticsService.getDashboardSummary());
    }

    @GetMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<List<GradeRecordDto>> getQuizAttempts(@PathVariable UUID quizId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findAll().stream()
            .filter(a -> a.getQuizId().equals(quizId))
            .collect(Collectors.toList());

        return ResponseEntity.ok(attempts.stream()
            .map(this::toDto)
            .collect(Collectors.toList()));
    }

    private GradeRecordDto toDto(QuizAttempt attempt) {
        return GradeRecordDto.builder()
            .quizId(attempt.getQuizId())
            .score(attempt.getScore())
            .maxScore(attempt.getMaxScore())
            .submittedAt(attempt.getSubmittedAt())
            .gradingLatencyMs(attempt.getGradingLatencyMs())
            .build();
    }
}
