package com.securemath.repository;

import com.securemath.domain.Lesson;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    
    List<Lesson> findByChapterIdOrderByPosition(UUID chapterId);
    
    long countByChapterId(UUID chapterId);

    @Query("SELECT COALESCE(MAX(l.position), -1) + 1 FROM Lesson l WHERE l.chapterId = :chapterId")
    int findNextPosition(@Param("chapterId") UUID chapterId);
}
