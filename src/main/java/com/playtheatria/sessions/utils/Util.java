package com.playtheatria.sessions.utils;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.playtheatria.sessions.database.data.Session;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class Util {
    public static final String COLOR_ONE = "#f5428a";
    public static final String COLOR_TWO = "#42f598";
    public static final String COLOR_THREE = "#fff8bd";
    public static final String PERMISSION_ALLOW = "theatria.sessions.allow";
    public static final String PERMISSION_ADMIN = "theatria.sessions.admin";
    public static final String PERMISSION_ACTIVITY_COMMAND = "theatria.sessions.activity.command";
    public static final String PERMISSION_COMMUNITY_COMMAND = "theatria.sessions.community.command";

    public static Component formatMessage(String label, Object value) {
        String template = "<color:%s>[<color:%s>%s<color:%s>]<color:%s> %s";
        String message =
                String.format(template, COLOR_ONE, COLOR_TWO, label, COLOR_ONE, COLOR_THREE, value);

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static void msg(String label, CommandSender sender) {
        sender.sendMessage(formatMessage(label, ""));
    }

    public static Component formatProgress(double sessionMinutes, double thresholdMinutes) {
        return Component.text(sessionMinutes + "/" + thresholdMinutes + " minutes");
    }

    public static Component formatIndicator(Session session) {
        String indicator = "❌";
        if (session.isRewarded()) {
            indicator = "✅";
        }
        return Component.text("[")
                .color(TextColor.fromHexString(Util.COLOR_ONE))
                .append(
                        Component.text(indicator)
                                .color(
                                        session.isRewarded()
                                                ? NamedTextColor.GREEN
                                                : NamedTextColor.DARK_RED))
                .append(Component.text("] ").color(TextColor.fromHexString(Util.COLOR_ONE)));
    }

    public static Component formatPlayerMessage(Session session) {
        double sessionMinutes =
                Math.floor(session.getSessionTime() / 6.0) / 10.0; // Truncate to 1 decimal place
        double thresholdMinutes = session.THRESHOLD / 60.0;
        return Util.formatIndicator(session)
                .append(
                        Util.formatProgress(sessionMinutes, thresholdMinutes)
                                .color(TextColor.fromHexString(COLOR_THREE)));
    }

    public static Component formatAdminMessage(Session session) {
        double sessionMinutes =
                Math.floor(session.getSessionTime() / 6.0) / 10.0; // Truncate to 1 decimal place
        double thresholdMinutes = session.THRESHOLD / 60.0;
        return Util.formatIndicator(session)
                .append(
                        Component.text(Util.formatToLengthWithEllipsis(session.getPlayerName(), 12))
                                .color(TextColor.fromHexString(Util.COLOR_TWO))
                                .append(
                                        Component.text(" ")
                                                .append(
                                                        Util.formatProgress(
                                                                sessionMinutes,
                                                                thresholdMinutes))));
    }

    public static String formatToLengthWithEllipsis(String input, int length) {
        if (input.length() > length) {
            // Truncate the string and add ellipsis (reserve space for the ellipsis)
            return input.substring(0, length - 3) + "...";
        }
        // Pad the string with spaces
        return String.format("%-" + length + "s", input);
    }

    public static String summary(@NotNull String prefix, @NotNull Session session) {
        return "[%s] %s".formatted(prefix, session);
    }
}
