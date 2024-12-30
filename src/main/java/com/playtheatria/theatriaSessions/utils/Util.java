package com.playtheatria.theatriaSessions.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

public class Util {
    public static Component formatMessage(String label, Object value) {
        return Component.text(label, NamedTextColor.AQUA)
                .append(Component.text(value.toString(), NamedTextColor.GOLD));
    }

    public static void sendFormattedLog(String message) {
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                "<color:#f5428a>[<color:#42f598>TheatriaSessions<color:#f5428a>] <color:#fff8bd>" + message));
    }
}
