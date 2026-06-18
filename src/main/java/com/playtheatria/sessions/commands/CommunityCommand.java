package com.playtheatria.sessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CommunityCommand implements CommandExecutor {

    private final DailyStatsService dailyStatsService;

    public CommunityCommand(DailyStatsService dailyStatsService) {
        this.dailyStatsService = dailyStatsService;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission(Util.PERMISSION_COMMUNITY_COMMAND)) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        int rewardCount = dailyStatsService.getRewardsEarned();

        sender.sendMessage(Util.formatMessage("Community", "Activity Goals"));
        sender.sendMessage(
                Component.text("Players at today's playtime goal: " + rewardCount)
                        .color(TextColor.fromHexString(Util.COLOR_THREE)));

        // Highest tier reached so far (Err when no tier has been unlocked yet).
        switch (RewardTier.getNearestTier(rewardCount)) {
            case Ok<RewardTier, Exception> ok -> {
                RewardTier current = ok.value();
                sender.sendMessage(
                        Component.text(
                                        "Active bonus: "
                                                + current.getDisplayName()
                                                + " (+"
                                                + current.getPercentage()
                                                + " sell hand)")
                                .color(TextColor.fromHexString(Util.COLOR_TWO)));
            }
            case Err<RewardTier, Exception> ignored -> sender.sendMessage(
                    Component.text("Active bonus: none unlocked yet today")
                            .color(TextColor.fromHexString(Util.COLOR_THREE)));
        }

        // Next tier to chase (Err when the top tier has already been reached).
        switch (RewardTier.getNextTier(rewardCount)) {
            case Ok<RewardTier, Exception> ok -> {
                RewardTier next = ok.value();
                int needed = next.getThreshold() - rewardCount;
                sender.sendMessage(
                        Component.text(
                                        needed
                                                + " more player(s) at the goal to unlock "
                                                + next.getDisplayName()
                                                + " (+"
                                                + next.getPercentage()
                                                + ")")
                                .color(TextColor.fromHexString(Util.COLOR_THREE)));
            }
            case Err<RewardTier, Exception> ignored -> sender.sendMessage(
                    Component.text("Top community tier reached!")
                            .color(TextColor.fromHexString(Util.COLOR_TWO)));
        }

        return true;
    }
}
