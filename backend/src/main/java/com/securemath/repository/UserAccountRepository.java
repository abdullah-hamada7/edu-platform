package com.securemath.repository;

import com.securemath.domain.UserAccount;
import com.securemath.domain.Role;
import com.securemath.domain.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    
    Optional<UserAccount> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByRole(Role role);
    
    long countByRoleAndStatus(Role role, AccountStatus status);
}
