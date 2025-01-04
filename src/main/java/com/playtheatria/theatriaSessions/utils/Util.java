package com.playtheatria.theatriaSessions.utils;

import com.playtheatria.theatriaSessions.data.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Util {
    public static final String COLOR_ONE = "#f5428a";
    public static final String COLOR_TWO = "#42f598";
    public static final String COLOR_THREE = "#fff8bd";

    public static Component formatMessage(String label, Object value) {
        String template = "<color:%s> %s";
        String message = String.format(template, COLOR_THREE, value);

        return formatLabel(label).append(MiniMessage.miniMessage().deserialize(message));
    }

    public static Component formatLabel(String label) {
        String template = "<color:%s>[<color:%s>%s<color:%s>]";
        String message = String.format(template, COLOR_ONE, COLOR_TWO, label, COLOR_ONE, COLOR_THREE);

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component formatProgressPlayerMessage(double sessionMinutes, double thresholdMinutes) {
        return Component.text(sessionMinutes + "/" + thresholdMinutes);
    }

    public static Component formatProgress(double sessionMinutes, double thresholdMinutes) {
        return Component.text(sessionMinutes + "/" + thresholdMinutes);
    }

    public static Component formatIndicator(boolean isRewarded) {
        return Component.text("[").color(TextColor.fromHexString(Util.COLOR_ONE))
                .append(
                        isRewarded ?
                                Component.text("✅").color(NamedTextColor.GREEN).hoverEvent(Component.text("Completed!")) :
                                Component.text("❌").color(NamedTextColor.RED).hoverEvent(Component.text("Incomplete")))
                .append(Component.text("] ").color(TextColor.fromHexString(Util.COLOR_ONE)));
    }

    public static Component formatPlayerMessage(Session session) {
        double sessionMinutes = Math.floor(session.getSessionTime() / 6.0) / 10.0; // Truncate to 1 decimal place
        double thresholdMinutes = session.THRESHOLD / 60.0;
        return Util.formatProgressPlayerMessage(sessionMinutes, thresholdMinutes).color(TextColor.fromHexString(COLOR_THREE))
                .append(Component.text(" "))
                .append(Util.formatIndicator(session.isRewarded()));
    }

    public static Component formatAdminMessage(Session session) {
        double sessionMinutes = Math.floor(session.getSessionTime() / 6.0) / 10.0; // Truncate to 1 decimal place
        double thresholdMinutes = session.THRESHOLD / 60.0;
        return Util.formatIndicator(session.isRewarded())
                .append(Component.text(Util.formatToLengthWithEllipsis(session.getPlayerName(), 12)).color(TextColor.fromHexString(Util.COLOR_TWO))
                        .append(Component.text(" ").append(Util.formatProgress(sessionMinutes, thresholdMinutes))));
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
