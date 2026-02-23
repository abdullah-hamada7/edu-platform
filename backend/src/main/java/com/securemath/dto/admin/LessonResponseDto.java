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
public class LessonResponseDto {
    
    private UUID id;
    private UUID chapterId;
    private String title;
    private UUID videoAssetId;
    private Integer position;
    private Instant createdAt;
}
