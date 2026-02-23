package com.securemath.repository;

import com.securemath.domain.Course;
import com.securemath.domain.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    
    List<Course> findByStatus(CourseStatus status);
    
    List<Course> findByStatusIn(List<CourseStatus> statuses);

    long countByStatus(CourseStatus status);
}
