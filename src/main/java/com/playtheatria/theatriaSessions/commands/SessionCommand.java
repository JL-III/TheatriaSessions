package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.data.ServerSession;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.enums.RewardTier;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.events.IncrementRewardCountEvent;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
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
    private final ServerSessionManager serverSessionManager;
    private final SessionManager sessionManager;
    private final ConfigManager configManager;
    private final String ADMIN_PERMISSION = "theatria.sessions.admin";

    public SessionCommand(ServerSessionManager serverSessionManager, SessionManager sessionManager, ConfigManager configManager) {
        this.serverSessionManager = serverSessionManager;
        this.sessionManager = sessionManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("theatria.sessions.allow")) return true;
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    ServerSession serverSession = serverSessionManager.getServerSession();
                    for (Session session : sessionManager.getSessions()) {
                        if (!session.getPlayerName().equalsIgnoreCase(player.getName())) continue;
                        sender.sendMessage(Util.formatLabel("----------Daily-Rewards----------"));
                        sender.sendMessage(Util.formatMessage("Date", serverSession.getSessionDate())
                                .append(Component.text(" "))
                                .append(Util.formatMessage("Players Joined", sessionManager.getSessions().size())));
                        sender.sendMessage(Util.formatLabel("Your Progress")
                                        .append(Component.text(" "))
                                        .append(Util.formatPlayerMessage(session))
                                .hoverEvent(
                                        session.isRewarded() ? Component.text("You earned your reward for today! This resets at 0:00 UTC") : Component.text("Keep playing to earn your reward and help the server meet it's goals!"))
                        );
                        for (RewardTier rewardTier : RewardTier.values()) {
                            sender.sendMessage(Util.formatLabel(rewardTier.getDisplayName())
                                    .append(Component.text(" "))
                                    .append(serverSession.getRewardsEarned() >= rewardTier.getThreshold() ? Util.formatIndicator(true) : Util.formatIndicator(false))
                                    .hoverEvent(Component.text("This unlocks a /sell hand boost when " + rewardTier.getThreshold() + " players have earned their /daily-reward!"))
                            );
                        }
                        sender.sendMessage(Util.formatLabel("Community")
                                        .append(Component.text(" "))
                                        .append(Util.formatMessage("Boost", RewardTier.getNearestTier(serverSession.getRewardsEarned()) != null ? RewardTier.getNearestTier(serverSession.getRewardsEarned()).getPercentage() : "0%")
                                        .append(Component.text(" "))
                                .append(Util.formatMessage("Progress", serverSession.getRewardsEarned()))
                                .hoverEvent(Component.text("This /sell hand boost resets every day at 0:00 UTC.\nCurrently " + serverSession.getRewardsEarned() + " players have earned their /daily-reward"))));
                        sender.sendMessage(Util.formatLabel("----------Daily-Rewards----------"));
                        return true;
                    }
                }
            }
            case 1 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "force-increment" -> {
                        Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
                    }
                    case "force-day-change" -> {
                        Bukkit.getPluginManager().callEvent(new DayChangeEvent());
                        return true;
                    }
//                    case "server-session" -> {
//                        ServerSession serverSession = serverSessionManager.getServerSession();
//                        sender.sendMessage(Util.formatMessage("Date", serverSession.getSessionDate()));
//                        sender.sendMessage(Util.formatMessage("PlayersJoined", sessionManager.getSessions().size()));
//                        for (RewardTier rewardTier : RewardTier.values()) {
//                            sender.sendMessage(Util.formatLabel(rewardTier.getDisplayName())
//                                    .append(serverSession.getRewardsEarned() >= rewardTier.getThreshold() ? Util.formatIndicator(true) : Util.formatIndicator(false))
//                                    .hoverEvent(Component.text("Unlocked when " + rewardTier.getThreshold() + " players have earned their /daily-reward!"))
//                            );
//                        }
//                        sender.sendMessage(Util.formatMessage("CurrentBoost", RewardTier.getNearestTier(serverSession.getRewardsEarned()) != null ? RewardTier.getNearestTier(serverSession.getRewardsEarned()).getPercentage() : "0%")
//                                .hoverEvent(Component.text("This is a /sell hand boost. This boost resets every day at 0:00UTC")));
//                        sender.sendMessage(Util.formatLabel("Hover over the text for more info")
//                                .hoverEvent(Component.text("Not this text, though."))
//                        );
//                    }
                    case "show-all" -> {
                        sender.sendMessage(Util.formatMessage("Number of Sessions", sessionManager.getSessions().size()));
                        for (Session session : sessionManager.getSessions()) {
                            sender.sendMessage(Util.formatAdminMessage(session));
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
                        "force-day-change",
                        "force-reward",
                        "reload-config",
                        "reset-progress",
                        "server-session",
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
