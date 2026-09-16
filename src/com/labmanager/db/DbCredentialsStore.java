package com.labmanager.db;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;
import java.util.Properties;

/**
 * Saves the MySQL host/port/username/password this computer should use to a
 * small file in the user's home folder, so the person is only ever asked for
 * their MySQL login once per machine. Every later launch on that same
 * computer loads the saved values automatically.
 *
 * The file lives at: ~/.labmanager/db.properties
 *
 * Note: the password is not strongly encrypted. That
 * keeps it out of plain sight in the file, but anyone with access to this
 * computer's file system and a little know-how could still recover it -
 * exactly like most small desktop apps that "remember" a local DB password.
 * Don't rely on this for a MySQL account that guards sensitive data.
 * But I have a feeeling a science lab manager's MySQL account is probably not that sensitive, so it's probably gonna be fine. Mr.NobodyX7777 ;)
 */
public class DbCredentialsStore {

    private static final Path FILE = Paths.get(System.getProperty("user.home"), ".labmanager", "db.properties");

    private DbCredentialsStore() {}

    public static boolean exists() {
        return Files.isRegularFile(FILE);
    }

    /** Loads the previously-saved credentials, or null if none are saved / the file is unreadable. */
    public static DbCredentials load() {
        if (!exists()) return null;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
        } catch (IOException e) {
            return null;
        }
        String host = p.getProperty("host", "localhost");
        String port = p.getProperty("port", "3306");
        String user = p.getProperty("user", "");
        String encodedPass = p.getProperty("password", "");
        String password;
        try {
            password = new String(Base64.getDecoder().decode(encodedPass), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            password = "";
        }
        return new DbCredentials(host, port, user, password);
    }

    /** Saves the given credentials, overwriting anything saved before. */
    public static void save(DbCredentials creds) throws IOException {
        Files.createDirectories(FILE.getParent());
        Properties p = new Properties();
        p.setProperty("host", creds.host);
        p.setProperty("port", creds.port);
        p.setProperty("user", creds.user);
        p.setProperty("password", Base64.getEncoder().encodeToString(creds.password.getBytes(StandardCharsets.UTF_8)));
        try (OutputStream out = Files.newOutputStream(FILE)) {
            p.store(out, "S Lab Sync MySQL connection details for this computer - do not share this file.");
        }
    }

    /** Removes any saved credentials (used by "Change MySQL Login"). */
    public static void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException ignored) {}
    }
}
