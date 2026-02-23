package com.securemath.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonCreateDto {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private UUID videoAssetId;
    
    private Integer position;
}
