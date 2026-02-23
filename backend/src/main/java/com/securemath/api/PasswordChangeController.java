package com.securemath.api;

import com.securemath.dto.auth.PasswordChangeRequestDto;
import com.securemath.domain.UserAccount;
import com.securemath.repository.UserAccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody PasswordChangeRequestDto request) {

        UUID userUuid = UUID.fromString(userId);
        UserAccount user = userAccountRepository.findById(userUuid)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userAccountRepository.save(user);

        return ResponseEntity.ok().body("Password changed successfully");
    }
}
