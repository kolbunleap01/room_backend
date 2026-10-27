package kh.edu.roomrental.room;

import java.util.Locale;

public enum RoomType {
    PRIVATE_ROOM,
    SHARED_ROOM,
    STUDIO,
    APARTMENT;

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim()
                .replaceAll("[\\s-]+", "_")
                .toUpperCase(Locale.ROOT);
        return valueOf(normalized).name();
    }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            normalize(value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
