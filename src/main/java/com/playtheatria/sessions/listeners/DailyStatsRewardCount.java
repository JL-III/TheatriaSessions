package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.IncrementRewardCountEvent;
import com.playtheatria.sessions.events.RewardCommunityEvent;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.utils.PLog;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DailyStatsRewardCount implements Listener {
    private final DailyStatsService dailyStatsService;
    private final PLog log;

    public DailyStatsRewardCount(DailyStatsService dailyStatsService, PLog log) {
        this.dailyStatsService = dailyStatsService;
        this.log = log;
    }

    @EventHandler
    public void onIncrementRewardCount(IncrementRewardCountEvent event) {
        int rewardCount = dailyStatsService.incrementRewardsEarned();
        RewardTier rewardTier = RewardTier.getByThreshold(rewardCount);

        if (rewardTier == null) {
            log.debug("No RewardTier found for rewardCount: " + rewardCount);
            return;
        }
        log.debug(
                String.format(
                        "Threshold found for reward count of: %s. Calling RewardCommunityEvent"
                                + " with: %s",
                        rewardCount, rewardTier));
        Bukkit.getPluginManager().callEvent(new RewardCommunityEvent(rewardTier));
    }
}
