package com.playtheatria.sessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.menus.Menu;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.Util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Single entry point for the plugin. With no args it shows the player's stats;
 * otherwise the first arg selects a {@link SubCommand} (activity, community,
 * streaks) or one of the admin verbs handled here.
 */
public class SessionCommand implements CommandExecutor, TabCompleter {
    private static final List<String> ADMIN_VERBS =
            List.of(
                    "check",
                    "create",
                    "force-reward",
                    "reload-config",
                    "reset-progress",
                    "set-progress",
                    "show-all");

    private final DailyStatsService dailyStatsService;
    private final SessionService sessionService;
    private final StreakService streakService;
    private final ConfigManager configManager;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public SessionCommand(
            DailyStatsService dailyStatsService,
            SessionService sessionService,
            StreakService streakService,
            ConfigManager configManager) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.streakService = streakService;
        this.configManager = configManager;

        register(new ActivitySubCommand(sessionService));
        register(new CommunitySubCommand(dailyStatsService));
        register(new StreaksSubCommand(streakService));
    }

    private void register(SubCommand sub) {
        subCommands.put(sub.name(), sub);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(Util.PERMISSION_ALLOW)) return true;
            if (sender instanceof Player player) {
                showStats(player);
            }
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub != null) {
            if (!sender.hasPermission(sub.permission())) return true;
            return sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return handleAdmin(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (SubCommand sub : subCommands.values()) {
                if (sender.hasPermission(sub.permission())) out.add(sub.name());
            }
            if (sender.hasPermission(Util.PERMISSION_ADMIN)) out.addAll(ADMIN_VERBS);
            return out;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub != null) {
            if (!sender.hasPermission(sub.permission())) return List.of();
            return sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return adminTabComplete(sender, args);
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return true;
        switch (args.length) {
            case 1 -> {
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

    private List<String> adminTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_ADMIN)) return List.of();
        switch (args.length) {
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

    private void showStats(Player player) {
        switch (sessionService.getSession(player.getUniqueId())) {
            case Ok<Session, Exception> ok -> {
                switch (streakService.getStreak(player.getUniqueId())) {
                    case Ok<Streak, Exception> okStreak -> sendSessionMessage(
                            player, ok.value(), okStreak.value());
                    case Err<Streak, Exception> errStreak -> player.sendMessage(
                            errStreak.error().getMessage());
                }
            }
            case Err<Session, Exception> err -> player.sendMessage(err.error().getMessage());
        }
    }

    public void sendSessionMessage(Player player, Session session, Streak streak) {
        TextColor fromHexString = TextColor.fromHexString(Util.COLOR_TWO);
        TextColor fromHexString2 = TextColor.fromHexString(Util.COLOR_THREE);
        final Menu menu =
                Menu.builder()
                        .themeColor(fromHexString)
                        .secondaryColor(fromHexString2)
                        .title(
                                Component.text(
                                        String.format(
                                                "Daily-Reward - %s",
                                                dailyStatsService.getDate().toString())))
                        .description(Component.text("Stats for server and personal progress"))
                        .entries("⭐ Community Stats")
                        .entries(
                                "   Players Joined:",
                                Menu.Entry.of(
                                                String.format(
                                                        " %s",
                                                        dailyStatsService.getPlayersJoined()))
                                        .description(
                                                "The number of players that have joined the"
                                                        + " server today. Supporter Rank and"
                                                        + " above can use /session activity to"
                                                        + " see who has joined today."))
                        .entries(
                                "   Players Earned:",
                                Menu.Entry.of(
                                        String.format(" %s", dailyStatsService.getRewardsEarned())))
                        .entries("⭐ Personal Stats")
                        .entries(
                                "   Progress:",
                                Menu.Entry.of(
                                                String.format(
                                                        " %s/%s",
                                                        session.getSessionTime(),
                                                        configManager.getRewardThreshold()))
                                        .description(
                                                "Your current progress towards"
                                                        + " earning your daily reward."))
                        .entries(
                                "   Earned Reward: ",
                                Menu.Entry.of(session.isRewarded() ? " ✅" : " ❌")
                                        .description(
                                                "Indicates whether you have earned your daily"
                                                        + " reward for today."))
                        .entries("⭐ Streaks")
                        // Current streak needs to be updated when the player logs in and or when
                        // they get their daily reward.
                        .entries(
                                "   Current Streak: ",
                                Menu.Entry.of(String.format(" %d days", streak.getCurrentStreak()))
                                        .description(
                                                "The number of consecutive days you have earned"
                                                        + " your daily reward."))
                        .entries(
                                "   Longest Streak: ",
                                Menu.Entry.of(String.format(" %d days", streak.getLongestStreak()))
                                        .description(
                                                "Your longest streak of consecutive days earning"
                                                        + " your daily reward."))
                        .entries(
                                "   Last Earned: ",
                                Menu.Entry.of(
                                                String.format(
                                                        " %s",
                                                        streak.getLastEarnedDate() == null
                                                                ? "N/A"
                                                                : streak.getLastEarnedDate()))
                                        .description("The last date you earned your daily reward."))
                        .build();
        player.sendMessage(menu.toComponent());
    }
}
