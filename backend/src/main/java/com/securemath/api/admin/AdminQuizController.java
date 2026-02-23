package com.securemath.api.admin;

import com.securemath.dto.admin.AdminQuestionCreateDto;
import com.securemath.dto.admin.AdminQuizCreateDto;
import com.securemath.dto.quiz.QuizSummaryDto;
import com.securemath.service.AdminQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminQuizController {

    private final AdminQuizService adminQuizService;

    @GetMapping("/courses/{courseId}/quizzes")
    public ResponseEntity<List<QuizSummaryDto>> listQuizzes(@PathVariable UUID courseId) {
        return ResponseEntity.ok(adminQuizService.listQuizzes(courseId));
    }

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizSummaryDto>> listAllQuizzes() {
        return ResponseEntity.ok(adminQuizService.listAllQuizzes());
    }

    @PostMapping("/courses/{courseId}/quizzes")
    public ResponseEntity<QuizSummaryDto> createQuiz(
            @PathVariable UUID courseId,
            @Valid @RequestBody AdminQuizCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminQuizService.createQuiz(courseId, dto));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<Void> addQuestion(
            @PathVariable UUID quizId,
            @Valid @RequestBody AdminQuestionCreateDto dto) {
        adminQuizService.addQuestion(quizId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/quizzes/{quizId}/publish")
    public ResponseEntity<Void> publishQuiz(@PathVariable UUID quizId) {
        adminQuizService.publishQuiz(quizId);
        return ResponseEntity.ok().build();
    }
}
