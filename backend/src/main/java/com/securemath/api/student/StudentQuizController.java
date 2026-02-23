package com.securemath.api.student;

import com.securemath.dto.quiz.*;
import com.securemath.service.StudentQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/quizzes")
@RequiredArgsConstructor
public class StudentQuizController {

    private final StudentQuizService studentQuizService;

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDetailDto> getQuiz(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal String userId) {
        UUID studentId = UUID.fromString(userId);
        return ResponseEntity.ok(studentQuizService.getQuizForStudent(quizId, studentId));
    }

    @GetMapping
    public ResponseEntity<List<QuizSummaryDto>> listQuizzesForCourse(
            @RequestParam UUID courseId,
            @AuthenticationPrincipal String userId) {
        UUID studentId = UUID.fromString(userId);
        return ResponseEntity.ok(studentQuizService.listQuizzesForCourse(courseId, studentId));
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizSubmissionResponseDto> submitQuiz(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody QuizSubmissionRequestDto request) {
        UUID studentId = UUID.fromString(userId);
        return ResponseEntity.ok(studentQuizService.submitQuiz(quizId, studentId, request));
    }
}
