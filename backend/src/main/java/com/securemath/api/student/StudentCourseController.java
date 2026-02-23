package com.securemath.api.student;

import com.securemath.domain.Course;
import com.securemath.domain.Enrollment;
import com.securemath.domain.EnrollmentStatus;
import com.securemath.dto.student.CourseListDto;
import com.securemath.repository.CourseRepository;
import com.securemath.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<List<CourseListDto>> listEnrolledCourses(@AuthenticationPrincipal String userId) {
        UUID studentId = UUID.fromString(userId);
        
        List<Enrollment> enrollments = enrollmentRepository
            .findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        
        List<CourseListDto> courses = enrollments.stream()
            .map(e -> courseRepository.findById(e.getCourseId()))
            .filter(opt -> opt.isPresent())
            .map(opt -> toDto(opt.get()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(courses);
    }

    private CourseListDto toDto(Course course) {
        return CourseListDto.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .build();
    }
}
