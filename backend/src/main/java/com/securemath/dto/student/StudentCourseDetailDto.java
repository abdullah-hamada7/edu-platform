package com.securemath.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCourseDetailDto {

    private UUID id;
    private String title;
    private String description;

    @Builder.Default
    private List<ChapterDto> chapters = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChapterDto {
        private UUID id;
        private String title;
        private Integer position;

        @Builder.Default
        private List<LessonDto> lessons = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonDto {
        private UUID id;
        private String title;
        private Integer position;
        private Boolean hasVideo;
    }
}
