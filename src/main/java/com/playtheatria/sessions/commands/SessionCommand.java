package com.playtheatria.sessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.Util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
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

public class SessionCommand implements CommandExecutor, TabCompleter {
    private final DailyStatsService dailyStatsService;

    private final SessionService sessionService;
    private final ConfigManager configManager;

    public SessionCommand(
            DailyStatsService dailyStatsService,
            SessionService sessionService,
            ConfigManager configManager) {
        this.dailyStatsService = dailyStatsService;

        this.sessionService = sessionService;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_ALLOW)) return true;
        switch (args.length) {
            case 0 -> {
                if (sender instanceof Player player) {
                    switch (sessionService.getSession(player.getUniqueId())) {
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
                if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return true;
                switch (args[0].toLowerCase()) {
                    case "show-all" -> {
                        sender.sendMessage(
                                Util.formatMessage(
                                        "Number of Sessions", sessionService.getSessionsCount()));
                        for (Session session : sessionService.getSessions()) {
                            sender.sendMessage(Util.formatAdminMessage(session, configManager));
                        }
                        return true;
                    }
                    case "reload-config" -> {
                        sender.sendMessage(Util.formatMessage("sessions", "Reloading config"));
                        configManager.reloadConfig();
                        return true;
                    }
                }
            }
            case 2 -> {
                if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return true;
                switch (args[0].toLowerCase()) {
                    case "check" -> {
                        DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss");
                        String formattedDate =
                                LocalDateTime.now(TimeUtils.timeZone).format(formatter);
                        for (Session session : sessionService.getSessions()) {
                            if (!session.getPlayerName().equalsIgnoreCase(args[1])) continue;
                            sender.sendMessage(Util.formatMessage("Date", formattedDate + " EST"));
                            sender.sendMessage(
                                    Util.formatMessage(
                                            "Progress",
                                            session.getSessionTime()
                                                    + "/"
                                                    + configManager.getRewardThreshold()));
                            sender.sendMessage(Util.formatMessage("AfkTime", session.getAfkTime()));
                            sender.sendMessage(
                                    Util.formatMessage("EarnedReward", session.isRewarded()));
                            return true;
                        }
                    }
                    case "force-reward" -> {
                        for (Session session : sessionService.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                Bukkit.getPluginManager()
                                        .callEvent(new RewardPlayerEvent(session.getPlayerUUID()));
                                return true;
                            }
                        }
                    }
                    case "reset-progress" -> {
                        for (Session session : sessionService.getSessions()) {
                            if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                session.setSessionTime(0);
                                return true;
                            }
                        }
                    }
                }
            }
            case 3 -> {
                if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return true;
                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        try {
                            Integer integer = Integer.valueOf(args[2]);
                            if (integer < 0)
                                throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionService.getSessions()) {
                                if (session.getPlayerName().equalsIgnoreCase(args[1])) {
                                    session.setSessionTime(integer);
                                    return true;
                                }
                            }
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                            if (offlinePlayer.getName() != null) {
                                Session session =
                                        new Session(
                                                offlinePlayer.getUniqueId(),
                                                offlinePlayer.getName());
                                session.setSessionTime(integer);
                                sessionService.addSession(session);
                                return true;
                            }
                            Util.msg(String.format("Player returned null: %s", args[1]), sender);
                        } catch (NumberFormatException exception) {
                            sender.sendMessage("Not a valid number: " + exception.getMessage());
                        }
                    }
                    case "set-progress" -> {
                        try {
                            Integer integer = Integer.valueOf(args[2]);
                            if (integer < 0)
                                throw new NumberFormatException("Must be higher than 0");
                            for (Session session : sessionService.getSessions()) {
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
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return List.of();
        switch (args.length) {
            case 1 -> {
                return List.of(
                        "check",
                        "create",
                        "force-reward",
                        "reload-config",
                        "reset-progress",
                        "set-progress",
                        "show-all");
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("show-all")) return List.of();
                return sessionService.getSessions().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
            case 3 -> {
                if (args[1].equalsIgnoreCase("set-progress") || args[1].equalsIgnoreCase("create"))
                    return List.of("<amount>");
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }

    public void sendSessionMessage(CommandSender sender, Session session) {
        Result<RewardTier, Exception> rewardTier =
                RewardTier.getNearestTier(dailyStatsService.getRewardsEarned());
        @SuppressWarnings("unused")
        String rewardTierName =
                rewardTier instanceof Ok<RewardTier, Exception> ok ? ok.value().name() : "0";
        @SuppressWarnings("unused")
        String rewardBonus =
                rewardTier instanceof Ok<RewardTier, Exception> ok
                        ? ok.value().getPercentage()
                        : "0%";
        TextColor textColorTwo = TextColor.fromHexString(Util.COLOR_TWO);
        TextColor textColorThree = TextColor.fromHexString(Util.COLOR_THREE);

        List.of(
                        Component.text(
                                        String.format(
                                                "Daily-Reward - %s",
                                                dailyStatsService.getDate().toString()))
                                .decorate(TextDecoration.UNDERLINED)
                                .color(textColorTwo),
                        Component.text("⭐ Community Stats").color(textColorTwo),
                        Component.text(
                                        String.format(
                                                "  • Players Joined %s",
                                                dailyStatsService.getPlayersJoined()))
                                .color(textColorThree)
                                .hoverEvent(
                                        HoverEvent.showText(
                                                Component.text(
                                                        "The number of players that have joined the"
                                                            + " server today. Supporter Rank and"
                                                            + " above can use /activity to see who"
                                                            + " has joined today."))),
                        Component.text(
                                        String.format(
                                                "  • Players Earned %s",
                                                dailyStatsService.getRewardsEarned()))
                                .color(textColorThree)
                                .hoverEvent(
                                        HoverEvent.showText(
                                                Component.text(
                                                        "The number of players that have earned a"
                                                                + " /daily-reward today."))),
                        //                Component.text(String.format("  • Community Reward Tier:
                        // %s", rewardTierName))
                        //                        .color(textColorThree)
                        //
                        // .hoverEvent(HoverEvent.showText(Component.text("The current reward tier
                        // for the community. More players earning their /daily-reward will increase
                        // this. The highest tier is 5."))),
                        //                Component.text(String.format("      • Community Sell Hand
                        // Bonus: %s", rewardBonus))
                        //                        .color(textColorThree)
                        //
                        // .hoverEvent(HoverEvent.showText(Component.text("The bonus players receive
                        // from the community reward tier when using /sell hand. This resets
                        // daily."))),
                        Component.text("⭐ Personal Stats").color(textColorTwo),
                        Component.text(
                                        String.format(
                                                "  • Progress: %s/%s",
                                                session.getSessionTime(),
                                                configManager.getRewardThreshold()))
                                .color(textColorThree),
                        Component.text("  • Earned Reward: ")
                                .color(textColorThree)
                                .append(Util.formatIndicator(session)))
                .forEach(sender::sendMessage);
    }
}
