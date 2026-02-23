package com.securemath.dto.student;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaybackGrantRequestDto {
    
    @NotBlank(message = "Device fingerprint is required")
    private String deviceFingerprint;
}
