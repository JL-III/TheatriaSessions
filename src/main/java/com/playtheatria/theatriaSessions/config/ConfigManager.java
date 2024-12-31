package com.playtheatria.theatriaSessions.config;

import com.playtheatria.theatriaSessions.TheatriaSessions;

public class ConfigManager {
    private final TheatriaSessions theatriaSessions;
    private boolean debug;
    private final long backupDuration;
    private final long initialBackupDuration;

    public ConfigManager(TheatriaSessions theatriaSessions) {
        this.theatriaSessions = theatriaSessions;
        this.debug = theatriaSessions.getConfig().getBoolean("debug");
        this.backupDuration = theatriaSessions.getConfig().getLong("backup-duration");
        this.initialBackupDuration = theatriaSessions.getConfig().getLong("initial-backup-duration");

    }

    public void reloadConfig() {
        theatriaSessions.reloadConfig();
        this.debug = theatriaSessions.getConfig().getBoolean("debug");
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
}
