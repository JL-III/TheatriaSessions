package com.playtheatria.theatriaSessions.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Util {
    public static final String COLOR_ONE = "#f5428a";
    public static final String COLOR_TWO = "#42f598";
    public static final String COLOR_THREE = "#fff8bd";

    public static Component formatMessage(String label, Object value) {
        String template = "<color:%s>[<color:%s>%s<color:%s>]<color:%s> %s";
        String message = String.format(template, COLOR_ONE, COLOR_TWO, label, COLOR_ONE, COLOR_THREE, value);

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static void sendFormattedLog(String message) {
        sendFormattedMessage(message, Bukkit.getConsoleSender());
    }

    public static void sendFormattedMessage(String message, CommandSender sender) {
        sender.sendMessage(formatMessage("TheatriaSessions", message));
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
