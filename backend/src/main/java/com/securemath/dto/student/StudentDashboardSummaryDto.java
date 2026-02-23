package com.securemath.dto.student;

import com.securemath.dto.quiz.GradeRecordDto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentDashboardSummaryDto {
    private int activeCoursesCount;
    private double averageScore;
    private String platformRank;
    private List<GradeRecordDto> recentActivity;
    private int weeklyProgressPercentage;
}
