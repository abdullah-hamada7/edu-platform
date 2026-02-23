package com.securemath.api.admin;

import com.securemath.domain.AccountStatus;
import com.securemath.domain.UserAccount;
import com.securemath.dto.admin.AccountStatusDto;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserAccountRepository userAccountRepository;

    @GetMapping
    public ResponseEntity<List<UserAccount>> listUsers() {
        return ResponseEntity.ok(userAccountRepository.findAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserAccount> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userAccountRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", userId)));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserAccount> updateStatus(
            @PathVariable UUID userId,
            @RequestBody AccountStatusDto dto) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        
        user.setStatus(AccountStatus.valueOf(dto.getStatus()));
        return ResponseEntity.ok(userAccountRepository.save(user));
    }
}
