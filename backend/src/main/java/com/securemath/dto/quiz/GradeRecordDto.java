package com.securemath.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRecordDto {
    
    private UUID quizId;
    private String quizTitle;
    private BigDecimal score;
    private BigDecimal maxScore;
    private Instant submittedAt;
    private Integer gradingLatencyMs;
}
