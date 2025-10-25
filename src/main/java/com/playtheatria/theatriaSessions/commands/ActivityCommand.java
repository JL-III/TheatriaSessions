package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ActivityCommand implements CommandExecutor {

    private final SessionManager sessionManager;

    public ActivityCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender commandSender,
            @NotNull Command command,
            @NotNull String s,
            @NotNull String[] args) {
        if (!commandSender.hasPermission("theatria.sessions.activity.command")) {
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
