package com.playtheatria.sessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.menus.Menu;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.Util;
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
 * Single entry point for the plugin. With no args it shows the player a unified
 * screen -- personal progress, streaks, community bonus tiers, and (for staff)
 * the roster of players who joined today with per-name detail on hover. The
 * first arg otherwise selects a {@link SubCommand} (streaks) or an admin verb.
 */
public class SessionCommand implements CommandExecutor, TabCompleter {
    private static final List<String> ADMIN_VERBS =
            List.of("create", "force-reward", "reload-config", "reset-progress", "set-progress");

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
                if (args[0].equalsIgnoreCase("reload-config")) {
                    sender.sendMessage(Util.formatMessage("sessions", "Reloading config"));
                    configManager.reloadConfig();
                    return true;
                }
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
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
                if (args[0].equalsIgnoreCase("reload-config")) return List.of();
                return sessionService.getSessions().stream()
                        .map(Session::getPlayerName)
                        .collect(Collectors.toList());
            }
            case 3 -> {
                if (args[0].equalsIgnoreCase("set-progress") || args[0].equalsIgnoreCase("create"))
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
        TextColor theme = TextColor.fromHexString(Util.COLOR_TWO);
        TextColor secondary = TextColor.fromHexString(Util.COLOR_THREE);

        int rewardCount = dailyStatsService.getRewardsEarned();
        String activeBonus = communityActiveBonus(rewardCount);
        String nextBonus = communityNextBonus(rewardCount);

        Menu.Builder builder =
                Menu.builder()
                        .themeColor(theme)
                        .secondaryColor(secondary)
                        .title(
                                Component.text(
                                        String.format(
                                                "Daily-Reward - %s", dailyStatsService.getDate())))
                        .description(
                                Component.text(
                                        "Your progress, streaks, and today's community activity."
                                                + " Hover any value for details."))
                        .entries("⭐ Personal Stats")
                        .entries(
                                "   Progress:",
                                Menu.Entry.of(
                                                String.format(
                                                        " %s/%s",
                                                        session.getSessionTime(),
                                                        configManager.getRewardThreshold()))
                                        .description(
                                                "Your current progress towards earning your daily"
                                                        + " reward."))
                        .entries(
                                "   Earned Reward:",
                                Menu.Entry.of(session.isRewarded() ? " ✅" : " ❌")
                                        .description(
                                                "Whether you have earned your daily reward today."))
                        .entries("⭐ Streaks")
                        .entries(
                                "   Current Streak:",
                                Menu.Entry.of(String.format(" %d days", streak.getCurrentStreak()))
                                        .description(
                                                "Consecutive days you have earned your daily"
                                                        + " reward."))
                        .entries(
                                "   Longest Streak:",
                                Menu.Entry.of(String.format(" %d days", streak.getLongestStreak()))
                                        .description("Your longest run of consecutive days."))
                        .entries(
                                "   Last Earned:",
                                Menu.Entry.of(
                                                String.format(
                                                        " %s",
                                                        streak.getLastEarnedDate() == null
                                                                ? "N/A"
                                                                : streak.getLastEarnedDate()))
                                        .description("The last day you earned your daily reward."))
                        .entries("⭐ Community")
                        .entries(
                                "   Players Joined:",
                                Menu.Entry.of(
                                                String.format(
                                                        " %s",
                                                        dailyStatsService.getPlayersJoined()))
                                        .description("Players who have joined the server today."))
                        .entries(
                                "   Players Earned:",
                                Menu.Entry.of(String.format(" %s", rewardCount))
                                        .description("Players who hit today's playtime goal."))
                        .entries(
                                "   Active Bonus:",
                                Menu.Entry.of(" " + activeBonus)
                                        .description(
                                                "The community sell-hand bonus active for everyone"
                                                        + " right now."))
                        .entries(
                                "   Next Bonus:",
                                Menu.Entry.of(" " + nextBonus)
                                        .description(
                                                "Progress towards unlocking the next community"
                                                        + " bonus tier."));

        // Staff-only roster: who joined today, with each player's detail on hover.
        // This folds the old activity/show-all/check views into one place.
        if (player.hasPermission(Util.PERMISSION_ACTIVITY_COMMAND)
                || player.hasPermission(Util.PERMISSION_ADMIN)) {
            builder.entries("⭐ Joined Today:", roster());
        }

        player.sendMessage(builder.build().toComponent());
    }

    private Menu.Entry[] roster() {
        List<Session> sessions = sessionService.getSessions();
        if (sessions.isEmpty()) {
            return new Menu.Entry[] {Menu.Entry.of("none yet")};
        }
        return sessions.stream()
                .map(session -> Menu.Entry.of(session.getPlayerName()).description(detail(session)))
                .toArray(Menu.Entry[]::new);
    }

    /** Per-player hover detail shown on each name in the roster (the old "check" view). */
    private String detail(Session session) {
        double sessionMinutes = Math.floor(session.getSessionTime() / 6.0) / 10.0;
        double thresholdMinutes = configManager.getRewardThreshold() / 60.0;
        return "Progress: "
                + sessionMinutes
                + "/"
                + thresholdMinutes
                + " min\nAFK: "
                + session.getAfkTime()
                + "\nEarned reward: "
                + (session.isRewarded() ? "yes" : "no");
    }

    private String communityActiveBonus(int rewardCount) {
        String active = "none unlocked yet today";
        switch (RewardTier.getNearestTier(rewardCount)) {
            case Ok<RewardTier, Exception> ok -> {
                RewardTier tier = ok.value();
                active = tier.getDisplayName() + " (+" + tier.getPercentage() + " sell hand)";
            }
            case Err<RewardTier, Exception> ignored -> {}
        }
        return active;
    }

    private String communityNextBonus(int rewardCount) {
        String next = "top tier reached!";
        switch (RewardTier.getNextTier(rewardCount)) {
            case Ok<RewardTier, Exception> ok -> {
                RewardTier tier = ok.value();
                int needed = tier.getThreshold() - rewardCount;
                next =
                        needed
                                + " more at the goal to unlock "
                                + tier.getDisplayName()
                                + " (+"
                                + tier.getPercentage()
                                + ")";
            }
            case Err<RewardTier, Exception> ignored -> {}
        }
        return next;
    }
}
