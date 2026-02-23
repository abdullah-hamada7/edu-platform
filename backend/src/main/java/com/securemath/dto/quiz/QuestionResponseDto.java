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
public class QuestionResponseDto {
    
    private UUID id;
    private String type;
    private String promptText;
    private Boolean latexEnabled;
    private Double points;
    private Integer position;
    private McqOptions mcqOptions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class McqOptions {
        private String[] options;
    }
}
