package com.playtheatria.sessions.config;

import com.playtheatria.sessions.TheatriaSessions;
import java.util.List;

public final class ConfigManager {
    private final TheatriaSessions plugin;
    private final long backupDuration;
    private final long initialBackupDuration;

    private List<String> rewards;
    private boolean communityRewardsEnabled;
    private String rewardMessage;
    public boolean debug;

    public ConfigManager(TheatriaSessions plugin) {
        this.plugin = plugin;
        this.backupDuration = plugin.getConfig().getLong("backup-duration");
        this.initialBackupDuration = plugin.getConfig().getLong("initial-backup-duration");
        this.rewardMessage = plugin.getConfig().getString("reward-message");
        this.debug = plugin.getConfig().getBoolean("debug");
        loadConfig();
    }

    public void loadConfig() {
        this.debug = plugin.getConfig().getBoolean("debug");
        this.rewards = plugin.getConfig().getStringList("rewards");
        this.communityRewardsEnabled = plugin.getConfig().getBoolean("community-rewards-enabled");
        this.rewardMessage = plugin.getConfig().getString("reward-message");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    public long getBackupDuration() {
        return this.backupDuration;
    }

    public long getInitDelay() {
        return this.initialBackupDuration;
    }

    public List<String> getRewards() {
        return this.rewards;
    }

    public boolean isCommunityRewardsEnabled() {
        return communityRewardsEnabled;
    }

    public String getRewardMessage() {
        return this.rewardMessage;
    }

    public boolean isDebug() {
        return debug;
    }
}
