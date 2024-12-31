package com.playtheatria.theatriaSessions.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Util {
    public static final String COLOR_ONE = "<color:#f5428a>";
    public static final String COLOR_TWO = "<color:#42f598>";
    public static final String COLOR_THREE = "<color:#fff8bd>";

    public static Component formatMessage(String label, Object value) {
        return MiniMessage.miniMessage().deserialize(
            String.format(COLOR_ONE + "[" + COLOR_TWO +"%s" + COLOR_ONE + "]" + COLOR_THREE + " %s", label, value)
        );
    }

    public static void sendFormattedLog(String message) {
        sendFormattedMessage(message, Bukkit.getConsoleSender());
    }

    public static void sendFormattedMessage(String message, CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                COLOR_ONE + "[" + COLOR_TWO + "TheatriaSessions" + COLOR_ONE + "] " + COLOR_THREE + message));
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
