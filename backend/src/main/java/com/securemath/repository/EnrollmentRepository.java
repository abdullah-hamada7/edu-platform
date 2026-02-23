package com.securemath.repository;

import com.securemath.domain.Enrollment;
import com.securemath.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    
    List<Enrollment> findByStudentIdAndStatus(UUID studentId, EnrollmentStatus status);
    
    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    
    Optional<Enrollment> findByStudentIdAndCourseIdAndStatus(UUID studentId, UUID courseId, EnrollmentStatus status);
    
    boolean existsByStudentIdAndCourseIdAndStatus(UUID studentId, UUID courseId, EnrollmentStatus status);
    
    List<Enrollment> findByCourseId(UUID courseId);
    
    List<Enrollment> findByCourseIdAndStatus(UUID courseId, EnrollmentStatus status);
}
