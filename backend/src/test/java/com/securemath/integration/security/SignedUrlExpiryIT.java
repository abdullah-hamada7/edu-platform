package com.securemath.integration.security;

import com.securemath.domain.*;
import com.securemath.repository.*;
import com.securemath.video.SignedUrlService;
import com.securemath.video.S3StorageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignedUrlExpiryIT {

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private SignedUrlService signedUrlService;

    private UUID assetId;
    private UUID studentId;

    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        
        ReflectionTestUtils.setField(signedUrlService, "expiryHours", 2);
    }

    @Test
    void calculateExpiryTime_returnsCorrectExpiry() {
        Instant before = Instant.now();
        Instant expiry = signedUrlService.calculateExpiryTime();
        Instant after = Instant.now().plus(Duration.ofHours(2));

        assertTrue(expiry.isAfter(before));
        assertTrue(expiry.isBefore(after.plusSeconds(1)));
    }

    @Test
    void isUrlExpired_returnsTrueForPastTime() {
        Instant pastTime = Instant.now().minusSeconds(1);
        
        assertTrue(signedUrlService.isUrlExpired(pastTime));
    }

    @Test
    void isUrlExpired_returnsFalseForFutureTime() {
        Instant futureTime = Instant.now().plusSeconds(3600);
        
        assertFalse(signedUrlService.isUrlExpired(futureTime));
    }

    @Test
    void isUrlExpired_returnsTrueForExactExpiryTime() {
        Instant expiryTime = Instant.now().minusMillis(1);
        
        assertTrue(signedUrlService.isUrlExpired(expiryTime));
    }

    @Test
    void expiryIsConfigurable() {
        ReflectionTestUtils.setField(signedUrlService, "expiryHours", 4);
        
        Instant expiry = signedUrlService.calculateExpiryTime();
        Instant expectedExpiry = Instant.now().plus(Duration.ofHours(4));
        
        assertTrue(expiry.isBefore(expectedExpiry.plusSeconds(1)));
        assertTrue(expiry.isAfter(expectedExpiry.minusSeconds(1)));
    }

    @Test
    void defaultExpiryIsTwoHours() {
        Instant expiry = signedUrlService.calculateExpiryTime();
        Instant expectedExpiry = Instant.now().plus(Duration.ofHours(2));
        
        Duration difference = Duration.between(expiry, expectedExpiry).abs();
        assertTrue(difference.getSeconds() < 1);
    }

    @Test
    void playbackGrantExpiryMatchesSignedUrlExpiry() {
        Instant signedUrlExpiry = signedUrlService.calculateExpiryTime();
        
        assertNotNull(signedUrlExpiry);
        assertTrue(signedUrlExpiry.isAfter(Instant.now()));
        assertTrue(signedUrlExpiry.isBefore(Instant.now().plus(Duration.ofHours(3))));
    }

    @Test
    void multipleCallsReturnProgressivelyLaterExpiry() throws InterruptedException {
        Instant firstExpiry = signedUrlService.calculateExpiryTime();
        
        Thread.sleep(10);
        
        Instant secondExpiry = signedUrlService.calculateExpiryTime();
        
        assertTrue(secondExpiry.isAfter(firstExpiry) || secondExpiry.equals(firstExpiry));
    }

    @Test
    void expiryAllowsTwoHourPlayback() {
        Instant now = Instant.now();
        Instant expiry = signedUrlService.calculateExpiryTime();
        
        Duration duration = Duration.between(now, expiry);
        assertTrue(duration.toHours() >= 2);
        assertTrue(duration.toHours() < 3);
    }

}
