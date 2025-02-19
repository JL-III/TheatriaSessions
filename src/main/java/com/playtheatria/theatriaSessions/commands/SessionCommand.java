package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.enums.RewardTier;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
    private final ServerSessionManager serverSessionManager;
    private final ConfigManager configManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;
    private final String ADMIN_PERMISSION = "theatria.sessions.admin";

    public SessionCommand(
            SessionManager sessionManager,
            ServerSessionManager serverSessionManager,
            ConfigManager configManager,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger
    ) {
        this.sessionManager = sessionManager;
        this.serverSessionManager = serverSessionManager;
        this.configManager = configManager;
        this.customLogger = customLogger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("theatria.sessions.allow")) return true;
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    switch (sessionManager.getSession(player.getUniqueId())) {
                        case Ok<Session, Exception> ok -> {
                            sendSessionMessage(player, ok.value());
                        }
                        case Err<Session, Exception> err -> {
                            player.sendMessage(err.error().getMessage());
                        }
                    }
                    return true;
                }
            }
            case 1 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "show-all" -> {
                        sender.sendMessage(Util.formatMessage("Number of Sessions", sessionManager.getSessions().size()));
                        for (Session session : sessionManager.getSessions().values()) {
                            sender.sendMessage(Util.formatAdminMessage(session));
                        }
                        return true;
                    }
                    case "reload-config" -> {
                        customLogger.sendFormattedMessage("Reloading config", sender);
                        configManager.reloadConfig();
                        return true;
                    }
                }
            }
            case 2 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "check" -> {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss");
                        String formattedDate = LocalDateTime.now(TimeUtils.timeZone).format(formatter);
                        for (Session session : sessionManager.getSessions().values()) {
                            if (!session.getPlayerName().equalsIgnoreCase(args[1])) continue;
                            sender.sendMessage(Util.formatMessage("Date", formattedDate + " EST"));
                            sender.sendMessage(Util.formatMessage("Progress", session.getSessionTime() + "/" + session.THRESHOLD));
                            sender.sendMessage(Util.formatMessage("AfkTime", session.getAfkTime()));
                            sender.sendMessage(Util.formatMessage("EarnedReward", session.isRewarded()));
                            return true;
                        }
                    }
                    case "force-reward" -> {
                        for (Session session : sessionManager.getSessions().values()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(session));
                                return true;
                            }
                        }
                    }
                    case "reset-progress" -> {
                        for (Session session : sessionManager.getSessions().values()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                session.setSessionTime(0);
                                return true;
                            }
                        }
                    }
                }
            }
            case 3 -> {
                if (!sender.hasPermission(ADMIN_PERMISSION)) return true;
                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        try {
                            Integer integer = Integer.parseInt(args[2]);
                            if (integer < 0) throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionManager.getSessions().values()) {
                                if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                    session.setSessionTime(integer);
                                    return true;
                                }
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                            if (offlinePlayer.getName() != null) {
                                Session session = new Session(offlinePlayer.getUniqueId(), offlinePlayer.getName());
                                session.setSessionTime(integer);
                                sessionManager.addSession(session);
                                return true;
                            }
                            customLogger.sendFormattedMessage(String.format("Player returned null: %s", args[1]), sender);
                        } catch (NumberFormatException exception) {
                            sender.sendMessage("Not a valid number: " + exception.getMessage());
                        }
                    }
                    case "set-progress" -> {
                        try {
                            Integer integer = Integer.parseInt(args[2]);
                            if (integer < 0) throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionManager.getSessions().values()) {
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
                        "create",
                        "force-reward",
                        "reload-config",
                        "reset-progress",
                        "set-progress",
                        "show-all"
                );
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("show-all")) return List.of();
                return sessionManager.getSessions().values().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase("set-progress") || args[1].equalsIgnoreCase("create")) return List.of("<amount>");
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }

    public void sendSessionMessage(CommandSender sender, Session session) {
        Result<RewardTier, Exception> rewardTier = RewardTier.getNearestTier(serverSessionManager.getServerSession().getRewardsEarned());
        List.of(
                Component.text(String.format("Daily-Reward - %s", serverSessionManager.getServerSession().getSessionDate().toString())).decorate(TextDecoration.UNDERLINED).color(TextColor.fromHexString(Util.COLOR_TWO)),
                Component.text("⭐ Community Stats").color(TextColor.fromHexString(Util.COLOR_TWO)),
                Component.text(String.format("  • Players Joined %s", serverSessionManager.getServerSession().getPlayersJoined())).color(TextColor.fromHexString(Util.COLOR_THREE))
                                .hoverEvent(HoverEvent.showText(Component.text("The number of unique players that have joined the server today."))),
                Component.text(String.format("  • Players Earned %s", serverSessionManager.getServerSession().getRewardsEarned())).color(TextColor.fromHexString(Util.COLOR_THREE))
                        .hoverEvent(HoverEvent.showText(Component.text("The number of players that have earned a /daily-reward today."))),
                Component.text(String.format("  • Community Reward Tier: %s", rewardTier instanceof Ok<RewardTier, Exception> ok ? ok.value().name() : "0")).color(TextColor.fromHexString(Util.COLOR_THREE))
                        .hoverEvent(HoverEvent.showText(Component.text("The current reward tier for the community. More players earning their /daily-reward will increase this."))),
                Component.text(String.format("      • Community Sell Hand Bonus: %s", rewardTier instanceof Ok<RewardTier, Exception> ok ? ok.value().getPercentage() : "0%")).color(TextColor.fromHexString(Util.COLOR_THREE))
                        .hoverEvent(HoverEvent.showText(Component.text("The bonus percentage players receive from the community reward tier when using /sell hand."))),
                Component.text("⭐ Personal Stats").color(TextColor.fromHexString(Util.COLOR_TWO)),
                Component.text(String.format("  • Progress: %s/%s", session.getSessionTime(), session.THRESHOLD)).color(TextColor.fromHexString(Util.COLOR_THREE)),
                Component.text("  • Earned Reward: ").color(TextColor.fromHexString(Util.COLOR_THREE)).append(Util.formatIndicator(session))

        ).forEach(sender::sendMessage);
    }
}
