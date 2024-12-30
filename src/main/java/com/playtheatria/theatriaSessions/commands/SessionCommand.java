package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.theatriaSessions.tasks.SessionTrackerTask;
import net.kyori.adventure.text.Component;
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

public class Session implements CommandExecutor, TabCompleter {
    private final SessionTrackerTask sessionTrackerTask;

    public Session(SessionTrackerTask sessionTrackerTask) {
        this.sessionTrackerTask =sessionTrackerTask;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    player.sendMessage(Component.text());
                }
            }
            case 1 -> {

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
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}
