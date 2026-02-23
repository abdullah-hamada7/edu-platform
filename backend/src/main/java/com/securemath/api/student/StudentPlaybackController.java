package com.securemath.api.student;

import com.securemath.dto.student.*;
import com.securemath.service.StudentPlaybackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/student/lessons")
@RequiredArgsConstructor
public class StudentPlaybackController {

    private final StudentPlaybackService studentPlaybackService;

    @PostMapping("/{lessonId}/playback-grant")
    public ResponseEntity<PlaybackGrantResponseDto> requestPlaybackGrant(
            @PathVariable UUID lessonId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody PlaybackGrantRequestDto request) {
        
        UUID studentId = UUID.fromString(userId);
        PlaybackGrantResponseDto grant = studentPlaybackService.requestPlaybackGrant(
            studentId, lessonId, request.getDeviceFingerprint());
        
        return ResponseEntity.ok(grant);
    }
}
