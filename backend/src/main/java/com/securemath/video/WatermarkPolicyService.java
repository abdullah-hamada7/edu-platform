package com.securemath.video;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

@Service
public class WatermarkPolicyService {

    private static final int WATERMARK_REFRESH_SECONDS_MIN = 15;
    private static final int WATERMARK_REFRESH_SECONDS_MAX = 30;

    public WatermarkPosition calculatePosition(UUID studentId, UUID lessonId, Instant currentTime) {
        long timestamp = currentTime.getEpochSecond();
        long period = WATERMARK_REFRESH_SECONDS_MAX;
        long offset = timestamp % period;
        
        int quadrant = (int) ((timestamp / WATERMARK_REFRESH_SECONDS_MIN) % 4);
        
        return WatermarkPosition.fromQuadrant(quadrant);
    }

    public String generateWatermarkSeed(UUID studentId, UUID lessonId) {
        String input = studentId.toString() + lessonId.toString() + Instant.now().getEpochSecond();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            return bytesToHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate watermark seed", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static class WatermarkPosition {
        private final double x;
        private final double y;

        public WatermarkPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() { return x; }
        public double getY() { return y; }

        public static WatermarkPosition fromQuadrant(int quadrant) {
            return switch (quadrant) {
                case 0 -> new WatermarkPosition(0.1, 0.1); // top-left
                case 1 -> new WatermarkPosition(0.8, 0.1); // top-right
                case 2 -> new WatermarkPosition(0.1, 0.8); // bottom-left
                case 3 -> new WatermarkPosition(0.8, 0.8); // bottom-right
                default -> new WatermarkPosition(0.5, 0.5); // center
            };
        }
    }
}
