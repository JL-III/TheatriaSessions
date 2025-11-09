package com.playtheatria.sessions.commands;

import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.managers.SessionCache;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ActivityCommand implements CommandExecutor {

    private final SessionCache sessionManager;

    public ActivityCommand(SessionCache sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String[] args) {
        if (!commandSender.hasPermission(Util.PERMISSION_ACTIVITY_COMMAND)) {
            commandSender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            commandSender.sendMessage(
                    Component.text("Players Joined Today:")
                            .color(TextColor.fromHexString(Util.COLOR_THREE)));
            for (Session session : sessionManager.getSessions().values()) {
                commandSender.sendMessage(
                        Component.text("  • " + session.getPlayerName())
                                .color(TextColor.fromHexString(Util.COLOR_TWO)));
            }
            return true;
        }

        return false;
    }
}
