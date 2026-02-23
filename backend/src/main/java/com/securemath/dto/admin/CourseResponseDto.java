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
public class CourseResponseDto {
    
    private UUID id;
    private String title;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
