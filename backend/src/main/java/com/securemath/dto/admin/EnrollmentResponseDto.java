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
public class EnrollmentResponseDto {
    
    private UUID id;
    private UUID studentId;
    private String studentEmail;
    private UUID courseId;
    private String courseTitle;
    private String status;
    private Instant enrolledAt;
}
