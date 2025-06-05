package de.syscall.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static String colorize(String text) {
        if (text == null) return "";
        return LEGACY_SERIALIZER.serialize(LEGACY_SERIALIZER.deserialize(text));
    }

    public static Component component(String text) {
        if (text == null) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(text);
    }
}