package com.securemath.video;

import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class HlsEncryptionService {

    public String generateEncryptionKeyRef(UUID assetId) {
        return "key/" + assetId + "/aes128.key";
    }

    public String generateEncryptionKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    public byte[] getEncryptionKey(String keyRef) {
        // In production, this would fetch from S3 or key management service
        // For now, generate a deterministic key from the reference
        return keyRef.getBytes();
    }
}
