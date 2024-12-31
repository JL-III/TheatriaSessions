package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.tasks.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SessionCommand implements CommandExecutor, TabCompleter {
    private final SessionManager sessionManager;
    private final ConfigManager configManager;
    private final String ADMIN_PERMISSION = "theatria.sessions.admin";

    public SessionCommand(SessionManager sessionManager, ConfigManager configManager) {
        this.sessionManager = sessionManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    String formattedDate = LocalDateTime.now().format(formatter);
                    for (Session session : sessionManager.getSessions()) {
                        if (!session.getPlayerName().equalsIgnoreCase(player.getName())) continue;
                        player.sendMessage(Util.formatMessage("Date", formattedDate + " UTC"));
                        player.sendMessage(Util.formatMessage("Progress", session.getSessionTime() + "/" + session.THRESHOLD));
                        player.sendMessage(Util.formatMessage("AfkTime", session.getAfkTime()));
                        player.sendMessage(Util.formatMessage("EarnedReward", session.isRewarded()));
                        return true;
                    }
                }
            }
            case 1 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "show-all" -> {
                        sender.sendMessage(Util.formatMessage("Number of Sessions", sessionManager.getSessions().size()));
                        for (Session session : sessionManager.getSessions()) {
                            String indicator = "❌";
                            if (session.isRewarded()) {
                                indicator = "✅";
                            }
                            sender.sendMessage(Component.text("[").color(TextColor.fromHexString(Util.COLOR_ONE))
                                            .append(Component.text(indicator).color(session.isRewarded() ? NamedTextColor.GREEN : NamedTextColor.DARK_RED)
                                            .append(Component.text("] ").color(TextColor.fromHexString(Util.COLOR_ONE))))
                                            .append(Component.text(Util.formatToLengthWithEllipsis(session.getPlayerName(), 12)).color(TextColor.fromHexString(Util.COLOR_TWO))
                                            .append(Component.text(" " + session.getSessionTime() + "/" + session.THRESHOLD).color(TextColor.fromHexString(Util.COLOR_THREE))
                                    )
                            ));
                        }
                        return true;
                    }
                    case "reload-config" -> {
                        Util.sendFormattedMessage("Reloading config", sender);
                        configManager.reloadConfig();
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
                                return true;
                            }
                        }
                    }
                    case "reset-progress" -> {
                        for (Session session : sessionManager.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                session.setSessionTime(0);
                                return true;
                            }
                        }
                    }
                    case "check" -> {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                        String formattedDate = LocalDateTime.now().format(formatter);
                        for (Session session : sessionManager.getSessions()) {
                            if (!session.getPlayerName().equalsIgnoreCase(args[1])) continue;
                            sender.sendMessage(Util.formatMessage("Date", formattedDate + " UTC"));
                            sender.sendMessage(Util.formatMessage("Progress", session.getSessionTime() + "/" + session.THRESHOLD));
                            sender.sendMessage(Util.formatMessage("AfkTime", session.getAfkTime()));
                            sender.sendMessage(Util.formatMessage("EarnedReward", session.isRewarded()));
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
                                    return true;
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
        if (!sender.hasPermission(ADMIN_PERMISSION)) return List.of();
        switch (args.length) {
            case 1 -> {
                return List.of(
                        "check",
                        "force-reward",
                        "reload-config",
                        "reset-progress",
                        "set-progress",
                        "show-all"
                );
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("show-all")) return List.of();
                return sessionManager.getSessions().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase("set-progress")) return List.of("<amount>");
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }
}
