package com.securemath.service;

import com.securemath.domain.*;
import com.securemath.dto.admin.*;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStudentService {

    private final UserAccountRepository userAccountRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public List<UserAccount> listStudents() {
        return userAccountRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.STUDENT)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserAccount updateAccountStatus(UUID userId, AccountStatus status) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        user.setStatus(status);
        return userAccountRepository.save(user);
    }

    @Transactional
    public EnrollmentResponseDto enrollStudent(UUID courseId, EnrollmentRequestDto dto) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
        
        UserAccount student = userAccountRepository.findById(dto.getStudentId())
            .orElseThrow(() -> ResourceNotFoundException.of("Student", dto.getStudentId()));
        
        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("User is not a student");
        }

        enrollmentRepository.findByStudentIdAndCourseId(dto.getStudentId(), courseId)
            .ifPresent(e -> {
                if (e.getStatus() == EnrollmentStatus.ACTIVE) {
                    throw new IllegalArgumentException("Student is already enrolled in this course");
                }
                e.setStatus(EnrollmentStatus.ACTIVE);
                enrollmentRepository.save(e);
            });

        Enrollment enrollment = Enrollment.builder()
            .studentId(dto.getStudentId())
            .courseId(courseId)
            .status(EnrollmentStatus.ACTIVE)
            .build();
        
        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponseDto(saved, student, course);
    }

    @Transactional
    public void removeEnrollment(UUID courseId, UUID studentId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        enrollment.setStatus(EnrollmentStatus.REMOVED);
        enrollmentRepository.save(enrollment);
    }

    public List<EnrollmentResponseDto> listCourseEnrollments(UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));

        return enrollmentRepository.findByCourseId(courseId).stream()
            .map(e -> {
                UserAccount student = userAccountRepository.findById(e.getStudentId()).orElse(null);
                return toResponseDto(e, student, course);
            })
            .collect(Collectors.toList());
    }

    private EnrollmentResponseDto toResponseDto(Enrollment enrollment, UserAccount student, Course course) {
        return EnrollmentResponseDto.builder()
            .id(enrollment.getId())
            .studentId(enrollment.getStudentId())
            .studentEmail(student != null ? student.getEmail() : null)
            .courseId(enrollment.getCourseId())
            .courseTitle(course.getTitle())
            .status(enrollment.getStatus().name())
            .enrolledAt(enrollment.getEnrolledAt())
            .build();
    }
}
