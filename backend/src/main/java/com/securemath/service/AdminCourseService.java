package com.securemath.service;

import com.securemath.domain.*;
import com.securemath.dto.admin.*;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final VideoAssetRepository videoAssetRepository;

    @Transactional
    public CourseResponseDto createCourse(CourseCreateDto dto) {
        Course course = Course.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .status(CourseStatus.DRAFT)
            .build();
        return toResponseDto(courseRepository.save(course));
    }

    public List<CourseResponseDto> listAllCourses() {
        return courseRepository.findAll().stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }

    public CourseDetailDto getCourseDetail(UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
        return toDetailDto(course);
    }

    @Transactional
    public CourseResponseDto updateCourse(UUID courseId, CourseUpdateDto dto) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        return toResponseDto(courseRepository.save(course));
    }

    @Transactional
    public CourseResponseDto publishCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
        course.setStatus(CourseStatus.PUBLISHED);
        return toResponseDto(courseRepository.save(course));
    }

    @Transactional
    public CourseResponseDto archiveCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
        course.setStatus(CourseStatus.ARCHIVED);
        return toResponseDto(courseRepository.save(course));
    }

    @Transactional
    public ChapterResponseDto createChapter(UUID courseId, ChapterCreateDto dto) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));
        
        int position = dto.getPosition() != null ? dto.getPosition() : (int) chapterRepository.countByCourseId(courseId);
        
        Chapter chapter = Chapter.builder()
            .courseId(courseId)
            .title(dto.getTitle())
            .position(position)
            .build();
        
        Chapter saved = chapterRepository.save(chapter);
        return ChapterResponseDto.builder()
            .id(saved.getId())
            .courseId(saved.getCourseId())
            .title(saved.getTitle())
            .position(saved.getPosition())
            .createdAt(saved.getCreatedAt())
            .build();
    }

    @Transactional
    public LessonResponseDto createLesson(UUID chapterId, LessonCreateDto dto) {
        Chapter chapter = chapterRepository.findById(chapterId)
            .orElseThrow(() -> ResourceNotFoundException.of("Chapter", chapterId));
        
        if (dto.getVideoAssetId() != null) {
            videoAssetRepository.findById(dto.getVideoAssetId())
                .orElseThrow(() -> ResourceNotFoundException.of("VideoAsset", dto.getVideoAssetId()));
        }
        
        int position = dto.getPosition() != null ? dto.getPosition() : (int) lessonRepository.countByChapterId(chapterId);
        
        Lesson lesson = Lesson.builder()
            .chapterId(chapterId)
            .title(dto.getTitle())
            .videoAssetId(dto.getVideoAssetId())
            .position(position)
            .build();
        
        Lesson saved = lessonRepository.save(lesson);
        return LessonResponseDto.builder()
            .id(saved.getId())
            .chapterId(saved.getChapterId())
            .title(saved.getTitle())
            .videoAssetId(saved.getVideoAssetId())
            .position(saved.getPosition())
            .createdAt(saved.getCreatedAt())
            .build();
    }

    private CourseResponseDto toResponseDto(Course course) {
        return CourseResponseDto.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .status(course.getStatus().name())
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .build();
    }

    private CourseDetailDto toDetailDto(Course course) {
        List<CourseDetailDto.ChapterDto> chapters = course.getChapters().stream()
            .map(ch -> CourseDetailDto.ChapterDto.builder()
                .id(ch.getId())
                .title(ch.getTitle())
                .position(ch.getPosition())
                .lessons(ch.getLessons().stream()
                    .map(le -> CourseDetailDto.LessonDto.builder()
                        .id(le.getId())
                        .title(le.getTitle())
                        .position(le.getPosition())
                        .hasVideo(le.getVideoAssetId() != null)
                        .build())
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());

        return CourseDetailDto.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .status(course.getStatus().name())
            .createdAt(course.getCreatedAt())
            .updatedAt(course.getUpdatedAt())
            .chapters(chapters)
            .build();
    }
}
