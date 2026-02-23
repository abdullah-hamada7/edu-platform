package com.securemath.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadRawVideo(UUID assetId, InputStream inputStream, long contentLength, String contentType) {
        String key = "raw/" + assetId + "/source.mp4";
        
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        log.info("Uploaded raw video to s3://{}/{}", bucketName, key);
        
        return key;
    }

    public String getHlsManifestKey(UUID assetId) {
        return "hls/" + assetId + "/playlist.m3u8";
    }

    public boolean objectExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    public String getBucketName() {
        return bucketName;
    }
}
