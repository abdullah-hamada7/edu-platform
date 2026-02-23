package com.securemath.repository;

import com.securemath.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    
    List<QuizAttempt> findByStudentId(UUID studentId);
    
    List<QuizAttempt> findByStudentIdOrderBySubmittedAtDesc(UUID studentId);
    
    Optional<QuizAttempt> findByQuizIdAndStudentId(UUID quizId, UUID studentId);
    
    boolean existsByQuizIdAndStudentId(UUID quizId, UUID studentId);
}
