package com.securemath.repository;

import com.securemath.domain.PlaybackAccessGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaybackAccessGrantRepository extends JpaRepository<PlaybackAccessGrant, UUID> {
    
    Optional<PlaybackAccessGrant> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);
    
    List<PlaybackAccessGrant> findByStudentId(UUID studentId);
}
