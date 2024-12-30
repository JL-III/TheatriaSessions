package com.playtheatria.theatriaSessions.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Util {
    public static Component formatMessage(String label, Object value) {
        return Component.text(label, NamedTextColor.AQUA)
                .append(Component.text(value.toString(), NamedTextColor.GOLD));
    }
    public static Component formatLog(Component component) {
        return Component.text("[TheatriaSessions] ").append(component);
    }
}
