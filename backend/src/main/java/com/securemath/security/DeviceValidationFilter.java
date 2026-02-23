package com.securemath.security;

import com.securemath.domain.RegisteredDevice;
import com.securemath.repository.RegisteredDeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DeviceValidationFilter extends OncePerRequestFilter {

    private final RegisteredDeviceRepository deviceRepository;
    
    @Value("${app.device-limit:2}")
    private int deviceLimit;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/student/") || path.contains("/login") || path.contains("/change-password");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String deviceFingerprint = extractDeviceFingerprint(request);
        if (deviceFingerprint == null) {
            sendErrorResponse(response, "Device fingerprint required");
            return;
        }

        String fingerprintHash = hashFingerprint(deviceFingerprint);
        UUID studentId = UUID.fromString(auth.getName());

        boolean isRegistered = deviceRepository.existsByStudentIdAndFingerprintHash(studentId, fingerprintHash);
        
        if (isRegistered) {
            filterChain.doFilter(request, response);
            return;
        }

        long deviceCount = deviceRepository.countByStudentId(studentId);
        if (deviceCount >= deviceLimit) {
            sendErrorResponse(response, "Maximum device limit reached (" + deviceLimit + " devices)");
            return;
        }

        RegisteredDevice newDevice = RegisteredDevice.builder()
            .studentId(studentId)
            .fingerprintHash(fingerprintHash)
            .lastSeenAt(Instant.now())
            .build();
        deviceRepository.save(newDevice);

        filterChain.doFilter(request, response);
    }

    private String extractDeviceFingerprint(HttpServletRequest request) {
        String header = request.getHeader("X-Device-Fingerprint");
        if (header != null && !header.isBlank()) {
            return header;
        }
        return request.getParameter("deviceFingerprint");
    }

    private String hashFingerprint(String fingerprint) {
        return UUID.nameUUIDFromBytes(fingerprint.getBytes()).toString();
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> error = Map.of("error", message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
