package com.playtheatria.theatriaSessions.config;

import com.playtheatria.jliii.generalutils.config.AbstractConfigManager;
import com.playtheatria.theatriaSessions.TheatriaSessions;

import java.util.List;

public class ConfigManager extends AbstractConfigManager<TheatriaSessions> {
    private final long backupDuration;
    private final long initialBackupDuration;

    private List<String> rewards;
    private boolean communityRewardsEnabled;

    public ConfigManager(TheatriaSessions plugin) {
        super(plugin);
        this.backupDuration = plugin.getConfig().getLong("backup-duration");
        this.initialBackupDuration = plugin.getConfig().getLong("initial-backup-duration");
        loadConfig();
    }

    @Override
    public void loadConfig() {
        this.debug = plugin.getConfig().getBoolean("debug");
        this.rewards = plugin.getConfig().getStringList("rewards");
        this.communityRewardsEnabled = plugin.getConfig().getBoolean("community-rewards-enabled");
    }

    public long getBackupDuration() {
        return this.backupDuration;
    }

    public long getInitialBackupDuration() {
        return this.initialBackupDuration;
    }

    public List<String> getRewards() {
        return this.rewards;
    }

    public boolean isCommunityRewardsEnabled() {
        return communityRewardsEnabled;
    }
}
