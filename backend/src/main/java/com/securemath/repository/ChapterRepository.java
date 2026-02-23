package com.securemath.repository;

import com.securemath.domain.Chapter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
    
    List<Chapter> findByCourseIdOrderByPosition(UUID courseId);
    
    long countByCourseId(UUID courseId);

    @Query("SELECT COALESCE(MAX(c.position), -1) + 1 FROM Chapter c WHERE c.courseId = :courseId")
    int findNextPosition(@Param("courseId") UUID courseId);
}
