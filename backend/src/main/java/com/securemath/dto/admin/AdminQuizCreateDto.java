package com.securemath.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminQuizCreateDto {

    @NotBlank(message = "Title is required")
    private String title;

    private Integer timeLimitSeconds;

    private String status;
}
