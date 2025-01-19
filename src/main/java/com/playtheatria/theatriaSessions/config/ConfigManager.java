package com.playtheatria.theatriaSessions.config;

import com.playtheatria.jliii.generalutils.config.AbstractConfigManager;
import com.playtheatria.theatriaSessions.TheatriaSessions;

import java.util.List;

public class ConfigManager extends AbstractConfigManager<TheatriaSessions> {
    private final TheatriaSessions plugin;
    private final long backupDuration;
    private final long initialBackupDuration;

    private List<String> rewards;
    private boolean debug;

    public ConfigManager(TheatriaSessions plugin) {
        super(plugin);
        this.plugin = plugin;
        this.backupDuration = plugin.getConfig().getLong("backup-duration");
        this.initialBackupDuration = plugin.getConfig().getLong("initial-backup-duration");
        loadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    public void loadConfig() {
        this.debug = plugin.getConfig().getBoolean("debug");
        this.rewards = plugin.getConfig().getStringList("rewards");
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
