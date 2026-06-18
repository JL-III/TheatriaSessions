package com.playtheatria.sessions.utils;

import com.playtheatria.sessions.database.data.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Util {
    public static final String COLOR_ONE = "#f5428a";
    public static final @NotNull String COLOR_TWO = "#42f598";
    public static final @NotNull String COLOR_THREE = "#fff8bd";
    public static final String PERMISSION_ALLOW = "theatria.sessions.allow";
    public static final String PERMISSION_ADMIN = "theatria.sessions.admin";
    public static final String PERMISSION_STREAKS_COMMAND = "theatria.streaks.allow";
    public static final String PERMISSION_STREAKS_ADMIN = "theatria.streaks.admin";
    public static final String PERMISSION_ACTIVITY_COMMAND = "theatria.sessions.activity.command";

    public static Component formatMessage(String label, Object value) {
        String template = "<color:%s>[<color:%s>%s<color:%s>]<color:%s> %s";
        String message =
                String.format(template, COLOR_ONE, COLOR_TWO, label, COLOR_ONE, COLOR_THREE, value);

        return MiniMessage.miniMessage().deserialize(message);
    }

    public static void msg(String label, CommandSender sender) {
        sender.sendMessage(formatMessage(label, ""));
    }

    public static String summary(@NotNull String prefix, @NotNull Session session) {
        return "[%s] %s".formatted(prefix, session);
    }

    public static String parseCommand(Player player, String rewardString) {
        return rewardString
                .replace("{player}", player.getName())
                .replace("{player_uuid}", player.getUniqueId().toString())
                .replace("{world}", player.getWorld().getName());
    }

    // --- Community sell-multiplier bonus (LuckPerms integration) ---
    // The group and duration are configuration (see config.yml: community-bonus);
    // these helpers only format the LuckPerms commands from the supplied values.

    /** LuckPerms command that grants a community bonus permission to the group. */
    public static String grantCommunityPermCommand(
            String permission, String group, String duration) {
        return "lp group "
                + group
                + " permission settemp "
                + permission
                + " true "
                + duration
                + " replace";
    }

    /** LuckPerms command that revokes a community bonus permission from the group. */
    public static String revokeCommunityPermCommand(String permission, String group) {
        return "lp group " + group + " permission unsettemp " + permission;
    }
}
