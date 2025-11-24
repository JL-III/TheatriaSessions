package com.playtheatria.sessions.config;

import com.playtheatria.sessions.TheatriaSessions;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigManager {
    private final TheatriaSessions plugin;
    private static final Logger log = Logger.getLogger("TheatriaSessions Config");
    private final long backupDuration;
    private final long initialBackupDuration;

    private List<String> rewards;
    private final NavigableMap<Integer, List<String>> streakRewards = new TreeMap<>();
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
        loadStreakRewards();
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

    public NavigableMap<Integer, List<String>> getStreakRewards() {
        return streakRewards;
    }

    private void loadStreakRewards() {
        streakRewards.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("streak-rewards");
        if (section == null) {
            log.log(Level.WARNING, "No 'streak-rewards' section in config.yml");
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                int streakValue = Integer.parseInt(key);
                List<String> commands = section.getStringList(key);
                if (commands.isEmpty()) {
                    log.log(Level.WARNING, "No commands configured for streak-rewards.{0}", key);
                    continue;
                }
                streakRewards.put(streakValue, commands);
            } catch (NumberFormatException e) {
                log.log(
                        Level.WARNING,
                        "Invalid streak-rewards key '" + key + "', expected integer",
                        e);
            }
        }
        log.log(Level.INFO, "Loaded {0} streak rewards", streakRewards.size());
    }
}
