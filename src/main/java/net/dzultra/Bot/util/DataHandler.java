package net.dzultra.Bot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataHandler {
    public static String getToken() {
        Path path = Path.of(System.getProperty("user.home"), ".ScryfallBot", "token");
        String token;
        try {
            token = Files.readString(path).trim();
        } catch (IOException e) {
            System.err.println("Error reading token file: " + e.getMessage());
            return null;
        }
        return token;
    }
}
