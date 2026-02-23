package com.securemath.service;

import com.securemath.domain.AccountStatus;
import com.securemath.domain.Role;
import com.securemath.domain.UserAccount;
import com.securemath.dto.admin.AdminUserCreateDto;
import com.securemath.dto.admin.AdminUserDto;
import com.securemath.exception.ResourceNotFoundException;
import com.securemath.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUserDto> listUsers() {
        return userAccountRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    public AdminUserDto getUser(UUID userId) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        return toDto(user);
    }

    @Transactional
    public AdminUserDto createUser(AdminUserCreateDto dto) {
        if (userAccountRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = Role.valueOf(dto.getRole());

        UserAccount user = UserAccount.builder()
            .email(dto.getEmail())
            .passwordHash(passwordEncoder.encode(dto.getTemporaryPassword()))
            .role(role)
            .status(AccountStatus.ACTIVE)
            .mustChangePassword(true)
            .build();

        return toDto(userAccountRepository.save(user));
    }

    @Transactional
    public AdminUserDto updateStatus(UUID userId, AccountStatus status) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        user.setStatus(status);
        return toDto(userAccountRepository.save(user));
    }

    private AdminUserDto toDto(UserAccount user) {
        return AdminUserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .status(user.getStatus().name())
            .mustChangePassword(user.getMustChangePassword())
            .build();
    }
}
