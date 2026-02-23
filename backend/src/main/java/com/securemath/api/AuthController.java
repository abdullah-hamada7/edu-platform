package com.securemath.api;

import com.securemath.dto.auth.LoginRequestDto;
import com.securemath.dto.auth.LoginResponseDto;
import com.securemath.domain.UserAccount;
import com.securemath.domain.AccountStatus;
import com.securemath.repository.UserAccountRepository;
import com.securemath.security.JwtTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserAccountRepository userAccountRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserAccount user = userAccountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

            if (user.getStatus() == AccountStatus.INACTIVE) {
                return ResponseEntity.status(403).body("Account is inactive");
            }

            String token = jwtTokenService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
            );

            return ResponseEntity.ok(LoginResponseDto.builder()
                .accessToken(token)
                .role(user.getRole().name())
                .mustChangePassword(user.getMustChangePassword())
                .build());

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
