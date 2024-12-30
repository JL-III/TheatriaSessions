package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.tasks.SessionManager;
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
                case 0,1 -> {
                    return false;
                }
                case 2 -> {
                    if (args[0].equalsIgnoreCase("force-reward")) {
                        for (Session session : sessionManager.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(session));
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
                        "reset-progress"
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
