package com.securemath.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoAssetResponseDto {

    private UUID id;
    private String rawObjectKey;
    private String hlsManifestKey;
    private String encryptionKeyRef;
    private String transcodeStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
