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
public class ChapterResponseDto {
    
    private UUID id;
    private UUID courseId;
    private String title;
    private Integer position;
    private Instant createdAt;
}
