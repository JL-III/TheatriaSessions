package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.enums.RewardTier;
import com.playtheatria.theatriaSessions.events.IncrementRewardCountEvent;
import com.playtheatria.theatriaSessions.events.RewardCommunityEvent;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IncrementRewardCountListener implements Listener {
    private final ServerSessionManager serverSessionManager;
    private final CustomLogger customLogger;

    public IncrementRewardCountListener(ServerSessionManager serverSessionManager, CustomLogger customLogger) {
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
        customLogger.sendDebug("Threshold found for: " + rewardCount + " calling RewardCommunityEvent!");
        Bukkit.getPluginManager().callEvent(new RewardCommunityEvent(rewardTier));
    }
}
