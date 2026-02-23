package com.securemath.service;

import com.securemath.domain.*;
import com.securemath.dto.student.CourseListDto;
import com.securemath.dto.student.StudentCourseDetailDto;
import com.securemath.exception.EnrollmentRequiredException;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentCourseService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;

    public List<CourseListDto> listEnrolledCourses(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository
            .findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);

        return enrollments.stream()
            .map(Enrollment::getCourseId)
            .map(courseRepository::findById)
            .filter(java.util.Optional::isPresent)
            .map(opt -> toListDto(opt.get()))
            .collect(Collectors.toList());
    }

    public StudentCourseDetailDto getCourseDetail(UUID studentId, UUID courseId) {
        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(studentId, courseId, EnrollmentStatus.ACTIVE)) {
            throw new EnrollmentRequiredException("Not enrolled in this course");
        }

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));

        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByPosition(courseId);

        List<StudentCourseDetailDto.ChapterDto> chapterDtos = chapters.stream()
            .map(chapter -> StudentCourseDetailDto.ChapterDto.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .position(chapter.getPosition())
                .lessons(lessonRepository.findByChapterIdOrderByPosition(chapter.getId()).stream()
                    .map(this::toLessonDto)
                    .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());

        return StudentCourseDetailDto.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .chapters(chapterDtos)
            .build();
    }

    private CourseListDto toListDto(Course course) {
        return CourseListDto.builder()
            .id(course.getId())
            .title(course.getTitle())
            .description(course.getDescription())
            .build();
    }

    private StudentCourseDetailDto.LessonDto toLessonDto(Lesson lesson) {
        return StudentCourseDetailDto.LessonDto.builder()
            .id(lesson.getId())
            .title(lesson.getTitle())
            .position(lesson.getPosition())
            .hasVideo(lesson.getVideoAssetId() != null)
            .build();
    }
}
