package com.penguin.client.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class StartupLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object LOCK = new Object();
    private static Path logPath;

    private StartupLogger() {
    }

    public static void init(String filename) {
        synchronized (LOCK) {
            if (logPath != null) {
                return;
            }
            try {
                Path logDir = Path.of("logs");
                Files.createDirectories(logDir);
                logPath = logDir.resolve(filename);
                writeLine("=== Startup log initialized ===");
            } catch (Exception e) {
                logPath = null;
                System.err.println("Failed to initialize startup logger: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void log(String message) {
        writeLine(message);
    }

    public static void logError(String message, Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));

        // Print to stderr
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.err.println("[" + timestamp + "] [ERROR] " + message);
        throwable.printStackTrace();

        // Write to file (and stdout)
        writeLine(message + System.lineSeparator() + sw);
    }

    private static void writeLine(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String formattedMessage = "[" + timestamp + "] " + message;

        // Print to stdout
        System.out.println(formattedMessage);

        Path path;
        synchronized (LOCK) {
            path = logPath;
        }
        if (path == null) {
            return;
        }

        try {
            Files.writeString(path, formattedMessage + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }
}
