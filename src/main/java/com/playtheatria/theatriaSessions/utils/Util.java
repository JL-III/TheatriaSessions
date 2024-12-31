package com.playtheatria.theatriaSessions.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

public class Util {
    public static Component formatMessage(String label, Object value) {
        return MiniMessage.miniMessage().deserialize(
            String.format("<color:#f5428a>[<color:#42f598>%s<color:#f5428a>] <color:#fff8bd> %s", label, value)
        );
    }

    public static void sendFormattedLog(String message) {
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                "<color:#f5428a>[<color:#42f598>TheatriaSessions<color:#f5428a>] <color:#fff8bd>" + message));
    }

    public static String formatToLengthWithEllipsis(String input, int length) {
        if (input.length() > length) {
            // Truncate the string and add ellipsis (reserve space for the ellipsis)
            return input.substring(0, length - 3) + "...";
        }
        // Pad the string with spaces
        return String.format("%-" + length + "s", input);
    }
}
