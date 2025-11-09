package com.playtheatria.sessions.commands;

import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.DailyStats;
import com.playtheatria.sessions.managers.DailyStatsCache;
import com.playtheatria.sessions.utils.Util;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommunityCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager configManager;
    private final DailyStatsCache dailyStatsCache;

    public CommunityCommand(ConfigManager configManager, DailyStatsCache dayStateCache) {
        this.configManager = configManager;
        this.dailyStatsCache = dayStateCache;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_COMMUNITY_COMMAND)) return true;
        switch (args.length) {
            case 0 -> {
                DailyStats dailyStats = dailyStatsCache.getDayStats();
                Util.msg("DailyStats", sender);
                Util.msg(String.format("Date: %s", dailyStats.getDate()), sender);
                Util.msg(String.format("RewardsEarned: %s", dailyStats.getRewardsEarned()), sender);
                Util.msg(String.format("PlayersJoined: %s", dailyStats.getPlayersJoined()), sender);
                Util.msg(String.format("isDebug: %s", configManager.isDebug()), sender);
                Util.msg(
                        String.format(
                                "isCommunityRewardsEnabled: %s",
                                configManager.isCommunityRewardsEnabled()),
                        sender);
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        return List.of();
    }
}
