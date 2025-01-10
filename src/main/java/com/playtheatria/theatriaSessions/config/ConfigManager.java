package com.playtheatria.theatriaSessions.config;

import com.playtheatria.theatriaSessions.TheatriaSessions;

import java.util.List;

public class ConfigManager {
    private final TheatriaSessions theatriaSessions;
    private final long backupDuration;
    private final long initialBackupDuration;

    private List<String> rewards;
    private boolean debug;

    public ConfigManager(TheatriaSessions theatriaSessions) {
        this.theatriaSessions = theatriaSessions;
        this.backupDuration = theatriaSessions.getConfig().getLong("backup-duration");
        this.initialBackupDuration = theatriaSessions.getConfig().getLong("initial-backup-duration");
        loadConfig();
    }

    public void reloadConfig() {
        theatriaSessions.reloadConfig();
        loadConfig();
    }

    public void loadConfig() {
        this.debug = theatriaSessions.getConfig().getBoolean("debug");
        this.rewards = theatriaSessions.getConfig().getStringList("rewards");
    }

    public boolean isDebug() {
        return this.debug;
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
}
