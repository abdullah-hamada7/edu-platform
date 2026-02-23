package com.securemath.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminQuestionCreateDto {

    @NotBlank(message = "Question type is required")
    private String type;

    @NotBlank(message = "Prompt text is required")
    private String promptText;

    private Boolean latexEnabled;

    @NotBlank(message = "Answer key is required")
    private String answerKey;

    @NotNull(message = "Points are required")
    private BigDecimal points;

    private Integer position;
}
