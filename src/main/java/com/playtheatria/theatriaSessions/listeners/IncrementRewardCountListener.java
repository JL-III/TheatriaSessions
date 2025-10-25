package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.enums.RewardTier;
import com.playtheatria.theatriaSessions.events.IncrementRewardCountEvent;
import com.playtheatria.theatriaSessions.events.RewardCommunityEvent;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IncrementRewardCountListener implements Listener {
    private final ServerSessionManager serverSessionManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public IncrementRewardCountListener(
            ServerSessionManager serverSessionManager,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.serverSessionManager = serverSessionManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onIncrementRewardCount(IncrementRewardCountEvent event) {
        serverSessionManager.getServerSession().incrementRewardsEarned();
        int rewardCount = serverSessionManager.getServerSession().getRewardsEarned();
        RewardTier rewardTier = RewardTier.getByThreshold(rewardCount);
        if (rewardTier == null) {
            customLogger.sendDebug("No RewardTier found for rewardCount: " + rewardCount);
            return;
        }
        customLogger.sendDebug(
                String.format(
                        "Threshold found for reward count of: %s. Calling RewardCommunityEvent"
                                + " with: %s",
                        rewardCount, rewardTier));
        Bukkit.getPluginManager().callEvent(new RewardCommunityEvent(rewardTier));
    }
}
