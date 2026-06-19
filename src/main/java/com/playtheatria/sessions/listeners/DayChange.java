package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import com.playtheatria.theatriaTime.events.DayChangeEvent;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DayChange implements Listener {
    private final DailyStatsService dailyStatsService;
    private final SessionService sessionService;
    private final ConfigManager configManager;
    private final PLog log;

    public DayChange(
            DailyStatsService dailyStatsService,
            SessionService sessionService,
            ConfigManager configManager,
            PLog log) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.configManager = configManager;
        this.log = log;
    }

    /**
     * Announces the day's roster to Discord before anything is reset.
     *
     * <p>Runs at {@link EventPriority#LOWEST} so it fires ahead of the NORMAL-priority reset
     * handlers below -- the session cache still holds every player who logged in today (sessions
     * are only pruned at the reset), so this is the last moment the full roster is available. The
     * announcement is dispatched as a console command so it works with any Discord bridge plugin
     * (see config.yml: discord-announce).
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDayChangeAnnounceToDiscord(DayChangeEvent event) {
        if (!configManager.isDiscordAnnounceEnabled()) return;

        String commandTemplate = configManager.getDiscordAnnounceCommand();
        if (commandTemplate.isBlank()) {
            log.warn(
                    "Discord announcements are enabled but 'discord-announce.command' is empty;"
                            + " skipping announcement.");
            return;
        }

        List<String> playerNames =
                sessionService.getSessions().stream()
                        .map(Session::getPlayerName)
                        .filter(Objects::nonNull)
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();

        String messageTemplate =
                playerNames.isEmpty()
                        ? configManager.getDiscordAnnounceEmptyMessage()
                        : configManager.getDiscordAnnounceMessage();
        if (messageTemplate.isBlank()) {
            log.debug(
                    "[DayChangeEvent] No Discord announcement message configured for this case;"
                            + " skipping.");
            return;
        }

        String command =
                Util.discordAnnounceCommand(
                        commandTemplate,
                        configManager.getDiscordAnnounceChannel(),
                        messageTemplate,
                        playerNames,
                        dailyStatsService.getDate().toString());

        if (Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)) {
            log.debugFmt("[DayChangeEvent] Dispatched Discord announcement: %s", command);
        } else {
            log.warn(
                    "Failed to dispatch Discord announcement command: '"
                            + command
                            + "'. Is your Discord bridge plugin installed and the configured"
                            + " 'discord-announce.command' correct?");
        }
    }

    /**
     * Handles DayChangeEvent to reset sessions and server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetSessions(DayChangeEvent event) {
        log.debug("[DayChangeEvent] Resetting sessions.");
        sessionService.reset();
    }

    /**
     * Handles DayChangeEvent to reset server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetDailyStats(DayChangeEvent event) {
        log.debug("[DayChangeEvent] resetting DailyStats.\nLogs for DailyStats");
        dailyStatsService.reset();
    }

    /**
     * Clears the community sell-multiplier bonuses at the daily reset so they never
     * carry across days. This is the authoritative terminator for the bonuses granted
     * in {@link RewardCommunity}; it fires even after downtime because TheatriaTime
     * detects the missed day change on restart. Revoking a node that was never granted
     * is a harmless no-op, so this runs regardless of whether the feature is enabled.
     */
    @EventHandler
    public void onDayChangeClearCommunityBonuses(DayChangeEvent event) {
        boolean allRevoked = true;
        for (RewardTier tier : RewardTier.values()) {
            if (!Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    Util.revokeCommunityPermCommand(
                            tier.getPermission(), configManager.getCommunityBonusGroup()))) {
                allRevoked = false;
            }
        }
        if (!allRevoked) {
            log.warn("Failed to revoke one or more community permissions. Is LuckPerms installed?");
        }
        log.debug("[DayChangeEvent] Cleared community sell-multiplier bonuses.");
    }
}
