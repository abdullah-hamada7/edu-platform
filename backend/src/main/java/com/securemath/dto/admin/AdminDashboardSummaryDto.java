package com.securemath.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardSummaryDto {
    private long totalStudents;
    private long activeCourses;
    private long totalExaminations;
    private String systemStatus;
    private double averagePerformance;
    private int storageUtilization;
}
