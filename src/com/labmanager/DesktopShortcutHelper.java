package com.labmanager;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DesktopShortcutHelper {

    private static final Path MARKER = Paths.get(System.getProperty("user.home"), ".labmanager", "shortcut_prompted");

    private DesktopShortcutHelper() {}

    public static void maybePromptForShortcut() {
        if (Files.exists(MARKER)) return;

        int choice = JOptionPane.showConfirmDialog(null,
                "Add a shortcut to S Lab Sync on your Desktop?",
                "S Lab Sync", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            createShortcut();
        }
        markPrompted();
    }

    private static void markPrompted() {
        try {
            Files.createDirectories(MARKER.getParent());
            Files.writeString(MARKER, "1");
        } catch (IOException ignored) {}
    }

    private static void createShortcut() {
        try {
            File jarFile = new File(DesktopShortcutHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!jarFile.getName().toLowerCase().endsWith(".jar")) return;
            File installDir = jarFile.getParentFile();
            File icoFile = new File(installDir, "logo.ico");
            File desktopDir = new File(System.getProperty("user.home"), "Desktop");
            File shortcutFile = new File(desktopDir, "S Lab Sync.lnk");
            String javawPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "javaw.exe";

            String cmd =
                    "$s=(New-Object -ComObject WScript.Shell).CreateShortcut(" + psQuote(shortcutFile.getAbsolutePath()) + ");" +
                    "$s.TargetPath=" + psQuote(javawPath) + ";" +
                    "$s.Arguments=" + psQuote("-jar \"" + jarFile.getAbsolutePath() + "\"") + ";" +
                    "$s.WorkingDirectory=" + psQuote(installDir.getAbsolutePath()) + ";" +
                    "$s.IconLocation=" + psQuote(icoFile.getAbsolutePath() + ",0") + ";" +
                    "$s.Save()";

            ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command", cmd);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            proc.waitFor();
        } catch (URISyntaxException | IOException | InterruptedException ignored) {}
    }

    private static String psQuote(String s) {
        return "'" + s.replace("'", "''") + "'";
    }
}
