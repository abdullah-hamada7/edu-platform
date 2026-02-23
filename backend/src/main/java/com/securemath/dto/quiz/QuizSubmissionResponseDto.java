package com.securemath.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmissionResponseDto {
    
    private UUID attemptId;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Integer gradingLatencyMs;
}
