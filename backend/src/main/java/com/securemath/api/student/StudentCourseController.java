package com.securemath.api.student;

import com.securemath.dto.student.CourseListDto;
import com.securemath.dto.student.StudentCourseDetailDto;
import com.securemath.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final StudentCourseService studentCourseService;

    @GetMapping
    public ResponseEntity<List<CourseListDto>> listEnrolledCourses(@AuthenticationPrincipal String userId) {
        UUID studentId = UUID.fromString(userId);
        return ResponseEntity.ok(studentCourseService.listEnrolledCourses(studentId));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<StudentCourseDetailDto> getCourseDetail(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal String userId) {
        UUID studentId = UUID.fromString(userId);
        return ResponseEntity.ok(studentCourseService.getCourseDetail(studentId, courseId));
    }
}
