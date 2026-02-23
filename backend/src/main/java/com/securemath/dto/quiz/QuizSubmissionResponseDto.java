package com.securemath.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmissionResponseDto {
    
    private UUID attemptId;
    private Double score;
    private Double maxScore;
    private Integer gradingLatencyMs;
}
