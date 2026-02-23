package com.securemath.service;

import com.securemath.domain.TranscodeStatus;
import com.securemath.domain.VideoAsset;
import com.securemath.dto.admin.VideoAssetResponseDto;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.VideoAssetRepository;
import com.securemath.video.HlsEncryptionService;
import com.securemath.video.HlsTranscodingService;
import com.securemath.video.S3StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVideoAssetService {

    private final VideoAssetRepository videoAssetRepository;
    private final S3StorageService s3StorageService;
    private final HlsTranscodingService hlsTranscodingService;
    private final HlsEncryptionService hlsEncryptionService;

    @Transactional
    public VideoAssetResponseDto uploadVideo(MultipartFile file) throws IOException {
        UUID assetId = UUID.randomUUID();
        String rawKey = s3StorageService.uploadRawVideo(
            assetId,
            file.getInputStream(),
            file.getSize(),
            file.getContentType() != null ? file.getContentType() : "video/mp4"
        );

        VideoAsset asset = VideoAsset.builder()
            .id(assetId)
            .rawObjectKey(rawKey)
            .hlsManifestKey(s3StorageService.getHlsManifestKey(assetId))
            .encryptionKeyRef(hlsEncryptionService.generateEncryptionKeyRef(assetId))
            .transcodeStatus(TranscodeStatus.PROCESSING)
            .build();

        VideoAsset saved = videoAssetRepository.save(asset);
        hlsTranscodingService.initiateTranscode(assetId, rawKey);

        return toDto(saved);
    }

    public List<VideoAssetResponseDto> listAssets() {
        return videoAssetRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public VideoAssetResponseDto refreshStatus(UUID assetId) {
        VideoAsset asset = videoAssetRepository.findById(assetId)
            .orElseThrow(() -> ResourceNotFoundException.of("VideoAsset", assetId));

        if (asset.getTranscodeStatus() == TranscodeStatus.PROCESSING
            && hlsTranscodingService.isTranscodeComplete(assetId)) {
            asset.setTranscodeStatus(TranscodeStatus.READY);
        }

        return toDto(videoAssetRepository.save(asset));
    }

    private VideoAssetResponseDto toDto(VideoAsset asset) {
        return VideoAssetResponseDto.builder()
            .id(asset.getId())
            .rawObjectKey(asset.getRawObjectKey())
            .hlsManifestKey(asset.getHlsManifestKey())
            .encryptionKeyRef(asset.getEncryptionKeyRef())
            .transcodeStatus(asset.getTranscodeStatus().name())
            .createdAt(asset.getCreatedAt())
            .updatedAt(asset.getUpdatedAt())
            .build();
    }
}
