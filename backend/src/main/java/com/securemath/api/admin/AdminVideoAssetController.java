package com.securemath.api.admin;

import com.securemath.dto.admin.VideoAssetResponseDto;
import com.securemath.service.AdminVideoAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/videos")
@RequiredArgsConstructor
public class AdminVideoAssetController {

    private final AdminVideoAssetService adminVideoAssetService;

    @PostMapping("/upload")
    public ResponseEntity<VideoAssetResponseDto> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminVideoAssetService.uploadVideo(file));
    }

    @GetMapping
    public ResponseEntity<List<VideoAssetResponseDto>> listAssets() {
        return ResponseEntity.ok(adminVideoAssetService.listAssets());
    }

    @PostMapping("/{assetId}/refresh")
    public ResponseEntity<VideoAssetResponseDto> refreshStatus(@PathVariable UUID assetId) {
        return ResponseEntity.ok(adminVideoAssetService.refreshStatus(assetId));
    }
}
