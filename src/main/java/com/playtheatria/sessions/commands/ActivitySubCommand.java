package com.playtheatria.sessions.commands;

import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

/** {@code /session activity} -- lists the players who have joined today. */
public class ActivitySubCommand implements SubCommand {

    private final SessionService sessionService;

    public ActivitySubCommand(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public String name() {
        return "activity";
    }

    @Override
    public String permission() {
        return Util.PERMISSION_ACTIVITY_COMMAND;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(
                Component.text("Players Joined Today:")
                        .color(TextColor.fromHexString(Util.COLOR_THREE)));
        for (Session session : sessionService.getSessions()) {
            sender.sendMessage(
                    Component.text("  • " + session.getPlayerName())
                            .color(TextColor.fromHexString(Util.COLOR_TWO)));
        }
        return true;
    }
}
