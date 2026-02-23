package com.securemath.repository;

import com.securemath.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    
    List<Question> findByQuizIdOrderByPosition(UUID quizId);
    
    long countByQuizId(UUID quizId);

    @Query("SELECT COALESCE(MAX(q.position), -1) + 1 FROM Question q WHERE q.quizId = :quizId")
    int findNextPosition(@Param("quizId") UUID quizId);
}
