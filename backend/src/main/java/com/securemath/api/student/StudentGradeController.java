package com.securemath.api.student;

import com.securemath.dto.quiz.GradeRecordDto;
import com.securemath.service.StudentQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/grades")
@RequiredArgsConstructor
public class StudentGradeController {

    private final StudentQuizService studentQuizService;

    @GetMapping
    public ResponseEntity<List<GradeRecordDto>> getGrades(@AuthenticationPrincipal String userId) {
        UUID studentId = UUID.fromString(userId);
        return ResponseEntity.ok(studentQuizService.getStudentGrades(studentId));
    }

    @GetMapping("/summary")
    public ResponseEntity<com.securemath.dto.student.StudentDashboardSummaryDto> getDashboardSummary(@AuthenticationPrincipal String userId) {
        UUID studentId = UUID.fromString(userId);
        return ResponseEntity.ok(studentQuizService.getDashboardSummary(studentId));
    }
}
