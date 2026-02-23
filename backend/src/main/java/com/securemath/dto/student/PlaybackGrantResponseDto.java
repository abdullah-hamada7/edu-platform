package com.securemath.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaybackGrantResponseDto {
    
    private String manifestUrl;
    private Instant expiresAt;
    private String watermarkSeed;
}
