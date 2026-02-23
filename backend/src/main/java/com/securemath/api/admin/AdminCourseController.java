package com.securemath.api.admin;

import com.securemath.dto.admin.*;
import com.securemath.service.AdminCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminCourseService.createCourse(dto));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> listCourses() {
        return ResponseEntity.ok(adminCourseService.listAllCourses());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDetailDto> getCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(adminCourseService.getCourseDetail(courseId));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseUpdateDto dto) {
        return ResponseEntity.ok(adminCourseService.updateCourse(courseId, dto));
    }

    @PostMapping("/{courseId}/publish")
    public ResponseEntity<CourseResponseDto> publishCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(adminCourseService.publishCourse(courseId));
    }

    @PostMapping("/{courseId}/archive")
    public ResponseEntity<CourseResponseDto> archiveCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(adminCourseService.archiveCourse(courseId));
    }

    @PostMapping("/{courseId}/chapters")
    public ResponseEntity<ChapterResponseDto> createChapter(
            @PathVariable UUID courseId,
            @Valid @RequestBody ChapterCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminCourseService.createChapter(courseId, dto));
    }

    @PostMapping("/chapters/{chapterId}/lessons")
    public ResponseEntity<LessonResponseDto> createLesson(
            @PathVariable UUID chapterId,
            @Valid @RequestBody LessonCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminCourseService.createLesson(chapterId, dto));
    }
}
