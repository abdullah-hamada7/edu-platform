package com.securemath.api.admin;

import com.securemath.domain.AccountStatus;
import com.securemath.dto.admin.AccountStatusDto;
import com.securemath.dto.admin.AdminUserCreateDto;
import com.securemath.dto.admin.AdminUserDto;
import com.securemath.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> listUsers() {
        return ResponseEntity.ok(adminUserService.listUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDto> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(adminUserService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<AdminUserDto> createUser(@Valid @RequestBody AdminUserCreateDto dto) {
        return ResponseEntity.ok(adminUserService.createUser(dto));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserDto> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody AccountStatusDto dto) {
        return ResponseEntity.ok(adminUserService.updateStatus(userId, AccountStatus.valueOf(dto.getStatus())));
    }
}
