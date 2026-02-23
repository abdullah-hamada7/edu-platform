package com.securemath.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class HlsTranscodingService {

    private final S3StorageService s3StorageService;

    public void initiateTranscode(UUID assetId, String rawObjectKey) {
        log.info("Transcoding initiated for asset {} from {}", assetId, rawObjectKey);
        // In production, this would trigger FFmpeg/transcode worker
        // For now, we log the action
    }

    public boolean isTranscodeComplete(UUID assetId) {
        String hlsKey = s3StorageService.getHlsManifestKey(assetId);
        return s3StorageService.objectExists(hlsKey);
    }

    public String getHlsManifestUrl(UUID assetId) {
        return s3StorageService.getHlsManifestKey(assetId);
    }
}
