package com.playtheatria.sessions.commands;

import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ActivityCommand implements CommandExecutor {

    private final SessionService sessionService;

    public ActivityCommand(SessionService sessionService) {
        this.sessionService = sessionService;
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
            for (Session session : sessionService.getSessions()) {
                commandSender.sendMessage(
                        Component.text("  • " + session.getPlayerName())
                                .color(TextColor.fromHexString(Util.COLOR_TWO)));
            }
            return true;
        }

        return false;
    }
}
