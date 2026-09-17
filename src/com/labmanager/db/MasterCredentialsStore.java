package com.labmanager.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

public class MasterCredentialsStore {

    private static final Path FILE = Paths.get(System.getProperty("user.home"), ".labmanager", "master.properties");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DEFAULT_USER = "Master";
    private static final String DEFAULT_PASS = "7777";

    private MasterCredentialsStore() {}

    public static boolean exists() {
        return Files.isRegularFile(FILE);
    }

    public static void ensureInitialized() {
        if (exists()) return;
        try {
            save(DEFAULT_USER, DEFAULT_PASS);
        } catch (IOException ignored) {}
    }

    public static String loadUsername() {
        Properties p = load();
        return p == null ? DEFAULT_USER : p.getProperty("username", DEFAULT_USER);
    }

    public static boolean verify(String username, String password) {
        Properties p = load();
        if (p == null) return false;
        String storedUser = p.getProperty("username", "");
        String salt = p.getProperty("salt", "");
        String storedHash = p.getProperty("hash", "");
        if (!storedUser.equals(username)) return false;
        return hash(password, salt).equals(storedHash);
    }

    public static void save(String username, String password) throws IOException {
        Files.createDirectories(FILE.getParent());
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        Properties p = new Properties();
        p.setProperty("username", username);
        p.setProperty("salt", salt);
        p.setProperty("hash", hash(password, salt));
        try (OutputStream out = Files.newOutputStream(FILE)) {
            p.store(out, null);
        }
    }

    private static Properties load() {
        if (!exists()) return null;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
        } catch (IOException e) {
            return null;
        }
        return p;
    }

    private static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
