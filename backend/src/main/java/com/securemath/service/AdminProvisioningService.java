package com.securemath.service;

import com.securemath.domain.UserAccount;
import com.securemath.domain.Role;
import com.securemath.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AdminProvisioningService implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-admin-password}")
    private String initialAdminPassword;

    @Override
    public void run(String... args) {
        if (!userAccountRepository.existsByRole(Role.ADMIN)) {
            log.info("No admin user found. Creating initial admin...");
            
            UserAccount admin = UserAccount.builder()
                .email("admin@securemath.local")
                .passwordHash(passwordEncoder.encode(initialAdminPassword))
                .role(Role.ADMIN)
                .mustChangePassword(true)
                .build();

            userAccountRepository.save(admin);
            log.info("Initial admin created with email: admin@securemath.local");
        }
    }
}
