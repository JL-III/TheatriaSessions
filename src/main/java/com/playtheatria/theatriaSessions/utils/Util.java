package com.playtheatria.theatriaSessions.utils;

import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.enums.HistoryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;
import java.util.List;

public class Util {
    public static final String COLOR_ONE = "#f5428a";
    public static final String COLOR_TWO = "#42f598";
    public static final String COLOR_THREE = "#fff8bd";

    public static Component formatMessage(String label, Object value) {
        String template = "<color:%s>[<color:%s>%s<color:%s>]<color:%s> %s";
        String message = String.format(template, COLOR_ONE, COLOR_TWO, label, COLOR_ONE, COLOR_THREE, value);

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component formatProgress(double sessionMinutes, double thresholdMinutes) {
        return Component.text(sessionMinutes + "/" + thresholdMinutes + " minutes");
    }

    public static Component formatIndicator(Session session) {
        String indicator = "❌";
        if (session.isRewarded()) {
            indicator = "✅";
        }
        return Component.text("[").color(TextColor.fromHexString(Util.COLOR_ONE))
                .append(Component.text(indicator).color(session.isRewarded() ? NamedTextColor.GREEN : NamedTextColor.DARK_RED))
                .append(Component.text("] ").color(TextColor.fromHexString(Util.COLOR_ONE)));
    }

    public static Component formatPlayerMessage(Session session) {
        double sessionMinutes = Math.floor(session.getSessionTime() / 6.0) / 10.0; // Truncate to 1 decimal place
        double thresholdMinutes = session.THRESHOLD / 60.0;
        return Util.formatIndicator(session).append(Util.formatProgress(sessionMinutes, thresholdMinutes).color(TextColor.fromHexString(COLOR_THREE)));
    }

    public static Component formatAdminMessage(Session session) {
        double sessionMinutes = Math.floor(session.getSessionTime() / 6.0) / 10.0; // Truncate to 1 decimal place
        double thresholdMinutes = session.THRESHOLD / 60.0;
        return Util.formatIndicator(session)
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

    public static String formatPrice(Price price) {
        return String.format(String.format("%s, %s, %s", price.getTimestamp().format(TimeUtils.getFormat()), price.getHistoryType(), price.getPrice()));
    }

    public static List<Price> getTestPriceList() {
        LocalDateTime localDateTime = LocalDateTime.now(TimeUtils.timeZone);
        return List.of(
                // The four hourly prices in a day
                new Price(HistoryType.HOURLY, localDateTime.minusHours(6), Material.DIAMOND, 105),
                new Price(HistoryType.HOURLY, localDateTime.minusHours(12), Material.DIAMOND, 110),
                new Price(HistoryType.HOURLY, localDateTime.minusHours(18), Material.DIAMOND, 95),
                new Price(HistoryType.HOURLY, localDateTime.minusHours(24), Material.DIAMOND, 100),

                // Daily
                new Price(HistoryType.DAILY, localDateTime.minusDays(7), Material.DIAMOND, 102),
                new Price(HistoryType.DAILY, localDateTime.minusDays(6), Material.DIAMOND, 104),
                new Price(HistoryType.DAILY, localDateTime.minusDays(5), Material.DIAMOND, 108),
                new Price(HistoryType.DAILY, localDateTime.minusDays(4), Material.DIAMOND, 96),
                new Price(HistoryType.DAILY, localDateTime.minusDays(3), Material.DIAMOND, 99),
                new Price(HistoryType.DAILY, localDateTime.minusDays(2), Material.DIAMOND, 103),
                new Price(HistoryType.DAILY, localDateTime.minusDays(1), Material.DIAMOND, 101),

                // Five weekly
                new Price(HistoryType.WEEKLY, localDateTime.minusMonths(5), Material.DIAMOND, 150),
                new Price(HistoryType.WEEKLY, localDateTime.minusMonths(4), Material.DIAMOND, 155),
                new Price(HistoryType.WEEKLY, localDateTime.minusMonths(3), Material.DIAMOND, 145),
                new Price(HistoryType.WEEKLY, localDateTime.minusMonths(2), Material.DIAMOND, 160),
                new Price(HistoryType.WEEKLY, localDateTime.minusMonths(1), Material.DIAMOND, 152),

                // Twelve monthly prices
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(12), Material.DIAMOND, 200),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(11), Material.DIAMOND, 195),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(10), Material.DIAMOND, 205),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(9), Material.DIAMOND, 210),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(8), Material.DIAMOND, 190),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(7), Material.DIAMOND, 215),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(6), Material.DIAMOND, 220),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(5), Material.DIAMOND, 225),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(4), Material.DIAMOND, 200),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(3), Material.DIAMOND, 210),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(2), Material.DIAMOND, 205),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(1), Material.DIAMOND, 195),
                new Price(HistoryType.MONTHLY, localDateTime.minusMonths(0), Material.DIAMOND, 215),

                // Five yearly
                new Price(HistoryType.YEARLY, localDateTime.minusYears(5), Material.DIAMOND, 300),
                new Price(HistoryType.YEARLY, localDateTime.minusYears(4), Material.DIAMOND, 310),
                new Price(HistoryType.YEARLY, localDateTime.minusYears(3), Material.DIAMOND, 290),
                new Price(HistoryType.YEARLY, localDateTime.minusYears(2), Material.DIAMOND, 320),
                new Price(HistoryType.YEARLY, localDateTime.minusYears(1), Material.DIAMOND, 305)
        );
    }
}
