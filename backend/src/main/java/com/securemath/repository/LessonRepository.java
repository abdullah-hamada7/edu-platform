package com.securemath.repository;

import com.securemath.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    
    List<Lesson> findByChapterIdOrderByPosition(UUID chapterId);
    
    long countByChapterId(UUID chapterId);
}
