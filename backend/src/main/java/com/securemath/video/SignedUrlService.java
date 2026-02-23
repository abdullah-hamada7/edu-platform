package com.securemath.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignedUrlService {

    private final S3Presigner s3Presigner;
    private final S3StorageService s3StorageService;

    @Value("${app.signed-url-expiry-hours:2}")
    private int expiryHours;

    public String generateSignedUrl(UUID assetId, UUID studentId) {
        String manifestKey = s3StorageService.getHlsManifestKey(assetId);
        
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(s3StorageService.getBucketName())
            .key(manifestKey)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(expiryHours))
            .getObjectRequest(getRequest)
            .build();

        URL signedUrl = s3Presigner.presignGetObject(presignRequest).url();
        log.debug("Generated signed URL for asset {} for student {}", assetId, studentId);
        
        return signedUrl.toString();
    }

    public Instant calculateExpiryTime() {
        return Instant.now().plus(Duration.ofHours(expiryHours));
    }

    public boolean isUrlExpired(Instant expiresAt) {
        return Instant.now().isAfter(expiresAt);
    }
}
