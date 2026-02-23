package com.securemath.repository;

import com.securemath.domain.RegisteredDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, UUID> {
    
    long countByStudentId(UUID studentId);
    
    Optional<RegisteredDevice> findByFingerprintHash(String fingerprintHash);
    
    boolean existsByStudentIdAndFingerprintHash(UUID studentId, String fingerprintHash);
    
    void deleteByStudentIdAndFingerprintHash(UUID studentId, String fingerprintHash);
}
