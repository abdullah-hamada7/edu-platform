package com.securemath.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDetailDto {
    
    private UUID id;
    private UUID courseId;
    private String title;
    private Integer timeLimitSeconds;
    private List<QuestionResponseDto> questions;
}
