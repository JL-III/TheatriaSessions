package com.playtheatria.sessions.config;

import com.playtheatria.sessions.TheatriaSessions;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigManager {
    private final TheatriaSessions plugin;
    private static final Logger log = Logger.getLogger("TheatriaSessions Config");
    public boolean debug;
    private final long backupDuration;
    private final long initialBackupDuration;

    private ConfigurationSection sessionSection;
    private int threshold;
    private List<String> rewards;
    private String rewardMessage;
    private String notifyMessage;

    private boolean communityRewardsEnabled;
    private String communityBonusGroup;
    private String communityBonusDuration;
    private int streakDelay;

    private ConfigurationSection streaksSection;
    private boolean streaksEnabled;
    private final NavigableMap<Integer, List<String>> streakRewards = new TreeMap<>();
    private final List<String> oracleLines = new ArrayList<>();
    private String encouragementMessage;

    public ConfigManager(TheatriaSessions plugin) {
        this.plugin = plugin;
        this.debug = plugin.getConfig().getBoolean("debug");
        this.backupDuration = plugin.getConfig().getLong("backup-duration");
        this.initialBackupDuration = plugin.getConfig().getLong("initial-backup-duration");
        loadConfig();
    }

    public void loadConfig() {
        this.debug = plugin.getConfig().getBoolean("debug");
        this.rewardMessage = plugin.getConfig().getString("reward-message");
        this.communityRewardsEnabled = plugin.getConfig().getBoolean("community-rewards-enabled");
        loadCommunityBonus();
        this.streakDelay = plugin.getConfig().getInt("streak-delay");
        this.sessionSection = plugin.getConfig().getConfigurationSection("session");
        loadThreshold();
        loadRewards();
        loadRewardMessage();
        loadNotifyMessage();
        this.streaksSection = plugin.getConfig().getConfigurationSection("streaks");
        loadSteaksEnabled();
        loadEncouragementMessage();
        loadStreakRewards();
        loadStreakRewardMessages();
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
        return this.communityRewardsEnabled;
    }

    public String getCommunityBonusGroup() {
        return this.communityBonusGroup;
    }

    public String getCommunityBonusDuration() {
        return this.communityBonusDuration;
    }

    public String getRewardMessage() {
        return this.rewardMessage;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public NavigableMap<Integer, List<String>> getStreakRewards() {
        return this.streakRewards;
    }

    public List<String> getOracleLines() {
        return this.oracleLines;
    }

    public String getEncouragementMessage() {
        return this.encouragementMessage;
    }

    public Integer getRewardThreshold() {
        return this.threshold;
    }

    public boolean isStreaksEnabled() {
        return this.streaksEnabled;
    }

    public String getNotifyMessage() {
        return this.notifyMessage;
    }

    public int getStreakDelay() {
        return this.streakDelay;
    }

    private void loadThreshold() {
        if (sessionSection == null) {
            log.log(Level.WARNING, "No 'session' section in config.yml");
            // default to 30 minutes if no config found, otherwise would default to 0.
            this.threshold = 1800;
            return;
        }
        this.threshold = sessionSection.getInt("threshold");
    }

    public void loadRewards() {
        if (sessionSection == null) {
            log.log(Level.WARNING, "No 'session' section in config.yml");
            // default to empty array.
            this.rewards = new ArrayList<>();
            return;
        }
        this.rewards = sessionSection.getStringList("rewards");
    }

    private void loadRewardMessage() {
        if (sessionSection == null) {
            log.log(Level.WARNING, "No 'session' section in config.yml");
            // default to empty array.
            this.rewardMessage = "";
            return;
        }
        this.rewardMessage = sessionSection.getString("reward-message");
    }

    private void loadNotifyMessage() {
        if (sessionSection == null) {
            log.log(Level.WARNING, "No 'session' section in config.yml");
            // default to empty array.
            this.notifyMessage = "";
            return;
        }
        this.notifyMessage = sessionSection.getString("notify-message");
    }

    private void loadCommunityBonus() {
        // Defaults guarantee the bonus still works (and the duration stays >24h) when
        // the section is missing; see the config.yml comments for why duration matters.
        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("community-bonus");
        if (section == null) {
            log.log(Level.WARNING, "No 'community-bonus' section in config.yml; using defaults");
            this.communityBonusGroup = "default";
            this.communityBonusDuration = "2d";
            return;
        }
        String group = section.getString("group");
        this.communityBonusGroup = (group == null || group.isEmpty()) ? "default" : group;
        String duration = section.getString("duration");
        this.communityBonusDuration = (duration == null || duration.isEmpty()) ? "2d" : duration;
    }

    private void loadSteaksEnabled() {
        if (streaksSection == null) {
            log.log(Level.WARNING, "No 'streaks' section in config.yml");
            return;
        }
        this.streaksEnabled = streaksSection.getBoolean("enabled");
    }

    private void loadStreakRewards() {
        streakRewards.clear();

        if (streaksSection == null) {
            log.log(Level.WARNING, "No 'streaks' section in config.yml");
            return;
        }

        ConfigurationSection rewardsSection = streaksSection.getConfigurationSection("rewards");
        if (rewardsSection == null) {
            log.log(Level.WARNING, "No 'streaks.rewards' section in config.yml");
            return;
        }

        for (String key : rewardsSection.getKeys(false)) {
            try {
                int streakValue = Integer.parseInt(key);
                List<String> commands = rewardsSection.getStringList(key);
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

    private void loadStreakRewardMessages() {
        oracleLines.clear();

        if (streaksSection == null) {
            log.log(Level.WARNING, "No 'streaks' section in config.yml");
            return;
        }

        List<String> lines = streaksSection.getStringList("reward-messages");
        if (lines.isEmpty()) {
            log.log(Level.WARNING, "No 'streaks.reward-messages' configured in config.yml");
            return;
        }
        oracleLines.addAll(lines);
        log.log(Level.INFO, "Loaded {0} streak reward messages", oracleLines.size());
    }

    private void loadEncouragementMessage() {
        if (streaksSection == null) {
            log.log(Level.WARNING, "No 'streaks' section in config.yml");
            return;
        }

        String message = streaksSection.getString("encouragement-message");
        if (message == null || message.isEmpty()) {
            log.log(Level.WARNING, "No 'streaks.encouragement-message' configured in config.yml");
            return;
        }
        this.encouragementMessage = message;
        log.log(Level.INFO, "Loaded encouragement message for streaks");
    }
}
