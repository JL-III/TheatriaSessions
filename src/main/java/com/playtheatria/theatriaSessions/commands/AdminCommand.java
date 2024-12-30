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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private final SessionManager sessionManager;

    public AdminCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("theatria.sessions.admin")) {
            switch (args.length) {
                case 0 -> {
                    return false;
                }
                case 1 -> {
                    switch (args[0].toLowerCase()) {
                        case "show-all" -> {
                            sender.sendMessage(Util.formatMessage("Number of Sessions: ", sessionManager.getSessions().size()));
                            for (Session session : sessionManager.getSessions()) {
                                sender.sendMessage(Util.formatMessage(" - ", session.getPlayerName() + " progress: " + session.getSessionTime() + "/" + session.THRESHOLD + " rewarded: " + session.isRewarded()));
                            }
                        }
                    }
                }
                case 2 -> {
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
                    }
                }
                case 3 -> {
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
            return true;
        }
        return false;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                return List.of(
                        "force-reward",
                        "reset-progress",
                        "set-progress",
                        "show-all"
                );
            }
            case 2 -> {
                return sessionManager.getSessions().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
