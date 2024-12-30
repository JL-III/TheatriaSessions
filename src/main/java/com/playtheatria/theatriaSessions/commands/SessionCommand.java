package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.tasks.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class SessionCommand implements CommandExecutor, TabCompleter {
    private final SessionManager sessionManager;

    public SessionCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    for (Session session : sessionManager.getSessions()) {
                        if (!session.getPlayerName().equals(player.getName())) continue;
                        player.sendMessage(Util.formatMessage("player: ", session.getPlayerName()));
                        player.sendMessage(Util.formatMessage("session: ", session.getSessionTime()));
                        player.sendMessage(Util.formatMessage("afkTime: ", session.getAfkTime()));
                        player.sendMessage(Util.formatMessage("threshold: ", session.THRESHOLD));
                        player.sendMessage(Util.formatMessage("hasEarnedReward: ", session.hasEarnedReward()));
                        player.sendMessage(Util.formatMessage("isRewarded: ", session.isRewarded()));
                        return true;
                    }
                }
            }
            case 1 -> {
                for (Session session : sessionManager.getSessions()) {
                    if (!session.getPlayerName().equals(args[0])) continue;
                    sender.sendMessage(Util.formatMessage("player: ", session.getPlayerName()));
                    sender.sendMessage(Util.formatMessage("session: ", session.getSessionTime()));
                    sender.sendMessage(Util.formatMessage("afkTime: ", session.getAfkTime()));
                    sender.sendMessage(Util.formatMessage("threshold: ", session.THRESHOLD));
                    sender.sendMessage(Util.formatMessage("hasEarnedReward: ", session.hasEarnedReward()));
                    sender.sendMessage(Util.formatMessage("isRewarded: ", session.isRewarded()));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> {
                return List.of();
            }
            case 1 -> {
                return sessionManager.getSessions().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
