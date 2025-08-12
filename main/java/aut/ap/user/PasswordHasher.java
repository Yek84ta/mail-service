package aut.ap.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";

    public String hashPassword(String password) {
        try {
            byte[] salt = generateSalt();
            byte[] hashedPassword = hashWithSalt(password, salt);
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedPassword = Base64.getDecoder().decode(parts[1]);
            byte[] hashedPassword = hashWithSalt(password, salt);
            return MessageDigest.isEqual(storedPassword, hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to verify password", e);
        }
    }

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        digest.update(salt);
        return digest.digest(password.getBytes());
    }
}