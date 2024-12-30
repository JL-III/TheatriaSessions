package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.tasks.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
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
    private final String ADMIN_PERMISSION = "theatria.sessions.admin";

    public SessionCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    for (Session session : sessionManager.getSessions()) {
                        if (!session.getPlayerName().equalsIgnoreCase(player.getName())) continue;
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
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "show-all" -> {
                        sender.sendMessage(Util.formatMessage("Number of Sessions: ", sessionManager.getSessions().size()));
                        for (Session session : sessionManager.getSessions()) {
                            sender.sendMessage(Util.formatMessage(" - ", session.getPlayerName() + " progress: " + session.getSessionTime() + "/" + session.THRESHOLD + " rewarded: " + session.isRewarded()));
                        }
                        return true;
                    }
                }
            }
            case 2 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "force-reward" -> {
                        for (Session session : sessionManager.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(session));
                            }
                        }
                    }
                    case "reset-progress" -> {
                        for (Session session : sessionManager.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                session.setSessionTime(0);
                            }
                        }
                    }
                    case "check" -> {
                        for (Session session : sessionManager.getSessions()) {
                            if (!session.getPlayerName().equalsIgnoreCase(args[1])) continue;
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
            }
            case 3 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "set-progress" -> {
                        try {
                            Integer integer = Integer.parseInt(args[2]);
                            if (integer < 0) throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionManager.getSessions()) {
                                if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                    session.setSessionTime(integer);
                                }
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Not a valid number: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return List.of();
                return List.of(
                        "check",
                        "force-reward",
                        "reset-progress",
                        "set-progress",
                        "show-all"
                );
            }
            case 2 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return List.of();
                return sessionManager.getSessions().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
            case 3 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return List.of();
                return List.of("<amount>");
            }
        }
        return List.of();
    }
}
