package com.securemath.repository;

import com.securemath.domain.VideoAsset;
import com.securemath.domain.TranscodeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoAssetRepository extends JpaRepository<VideoAsset, UUID> {
    
    List<VideoAsset> findByTranscodeStatus(TranscodeStatus status);
}
