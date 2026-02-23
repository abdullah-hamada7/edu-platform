package com.securemath.dto.quiz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmissionRequestDto {
    
    @Valid
    @NotNull(message = "Answers are required")
    private List<AnswerDto> answers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnswerDto {
        
        @NotNull(message = "Question ID is required")
        private UUID questionId;
        
        @NotNull(message = "Response is required")
        private String response;
    }
}
