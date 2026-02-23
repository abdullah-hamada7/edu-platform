package com.securemath.api.admin;

import com.securemath.domain.Course;
import com.securemath.domain.Enrollment;
import com.securemath.domain.EnrollmentStatus;
import com.securemath.domain.UserAccount;
import com.securemath.dto.admin.EnrollmentRequestDto;
import com.securemath.dto.admin.EnrollmentResponseDto;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.CourseRepository;
import com.securemath.repository.EnrollmentRepository;
import com.securemath.repository.UserAccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/courses/{courseId}/enrollments")
@RequiredArgsConstructor
public class AdminEnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final CourseRepository courseRepository;

    @GetMapping
    public ResponseEntity<List<EnrollmentResponseDto>> listEnrollments(@PathVariable UUID courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return ResponseEntity.ok(enrollments.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponseDto> enrollStudent(
            @PathVariable UUID courseId,
            @Valid @RequestBody EnrollmentRequestDto dto) {
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
        
        UserAccount student = userAccountRepository.findById(dto.getStudentId())
            .orElseThrow(() -> ResourceNotFoundException.of("Student", dto.getStudentId()));
        
        if (enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), courseId, EnrollmentStatus.ACTIVE)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        Enrollment enrollment = Enrollment.builder()
            .studentId(student.getId())
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toResponseDto(enrollmentRepository.save(enrollment)));
    }

    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> removeEnrollment(
            @PathVariable UUID courseId,
            @PathVariable UUID enrollmentId) {
        
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> ResourceNotFoundException.of("Enrollment", enrollmentId));
        
        enrollment.setStatus(EnrollmentStatus.REMOVED);
        enrollmentRepository.save(enrollment);
        
        return ResponseEntity.noContent().build();
    }

    private EnrollmentResponseDto toResponseDto(Enrollment enrollment) {
        return EnrollmentResponseDto.builder()
            .id(enrollment.getId())
            .studentId(enrollment.getStudentId())
            .courseId(enrollment.getCourseId())
            .status(enrollment.getStatus().name())
            .enrolledAt(enrollment.getEnrolledAt())
            .build();
    }
}
