package com.playtheatria.sessions.service;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.cache.StreakCache;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.database.repositories.StreakRepo;
import com.playtheatria.sessions.enums.StreakOutcome;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StreakService {
    private final StreakCache cache;
    private final StreakRepo repo;
    private final ConfigManager cm;
    private final PLog log;

    public StreakService(StreakCache cache, StreakRepo repo, ConfigManager cm, PLog log) {
        this.cache = cache;
        this.repo = repo;
        this.cm = cm;
        this.log = log;
    }

    public Result<Streak, Exception> getStreak(UUID playerUUUID) {
        return cache.getStreak(playerUUUID);
    }

    public boolean hasStreak(UUID playerUUID) {
        return cache.hasStreak(playerUUID);
    }

    public void createNewStreak(UUID playerUUID, String playerName) {
        cache.createNewStreak(playerUUID, playerName);
    }

    public Collection<Streak> getStreaks() {
        return cache.getStreaks().values();
    }

    public void setStreak(@NotNull Streak streak) {
        cache.setStreak(streak);
    }

    /**
     * Persist all streaks in the cache to the database.
     * @param verbose whether to log each persisted streak verbosely.
     */
    public void persist(boolean verbose) {
        log.debugFmt("[persist] Running on thread: %s", Thread.currentThread().getName());
        for (Streak streak : getStreaks()) {
            switch (repo.createOrUpdate(streak)) {
                case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> {
                    Dao.CreateOrUpdateStatus status = ok.value();
                    String msg =
                            String.format(
                                    "Persisted streak - Created: %b, Updated: %b, Lines changed:"
                                            + " %d. ",
                                    status.isCreated(),
                                    status.isUpdated(),
                                    status.getNumLinesChanged());
                    if (verbose) {
                        log.info(msg + streak);
                    } else {
                        log.debug(msg + streak);
                    }
                }
                case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> log.err(
                        String.format(
                                "Persisting streak %s failed: %s",
                                streak, err.error().getMessage()));
            }
        }
    }

    /**
     * Handles the logic for incrementing a player's streak and rewarding them if applicable.
     * @param playerUUID the UUID of the player whose streak we are handling.
     * @param player the Player object representing the player.
     */
    public void handleStreak(UUID playerUUID, Player player) {
        switch (getStreak(playerUUID)) {
            case Ok<Streak, Exception> ok -> {
                LocalDate today = LocalDate.now(TimeUtils.timeZone);
                try {
                    LocalDate lastDate = ok.value().getLastEarnedDate();
                    StreakOutcome outcome = determineOutcome(lastDate, today);

                    switch (outcome) {
                        case NO_PREVIOUS, CONSECUTIVE -> handleStreakIncrement(
                                ok.value(), player, today);
                        case SAME_DAY -> log.debugFmt(
                                "Player %s already rewarded today", player.getName());
                        case BROKEN -> {
                            // if the last date is before yesterday, reset the streak to 1
                            handleBrokenStreak(ok.value(), player, today);
                        }
                    }
                } catch (DateTimeParseException dtpe) {
                    log.err(
                            String.format(
                                    "Failed to parse last earned date for player %s: %s setting"
                                            + " current streak to 1.",
                                    player.getName(), dtpe.getMessage()));
                    handleBrokenStreak(ok.value(), player, today);
                }
            }
            case Err<Streak, Exception> err -> log.err(
                    String.format(
                            "Failed to increment streak for player %s due to error: %s",
                            player.getName(), err.error().getMessage()));
        }
    }

    /**
     * Handles a broken streak by resetting the current streak to one and updating the last earned date.
     * @param streak the Streak object representing the player's streak.
     * @param player the Player object representing the player.
     * @param today the current date.
     */
    private void handleBrokenStreak(Streak streak, Player player, LocalDate today) {
        streak.setCurrentStreakToOne();
        streak.setLastEarnedDate(today);
        log.debugFmt("Streak broken. Resetting streak for %s", player.getName());
    }

    /**
     * Determines the outcome of the streak based on the last earned date and today's date.
     * @param last the last date the player earned a streak.
     * @param today today's date.
     * @return the StreakOutcome representing the result.
     */
    private StreakOutcome determineOutcome(LocalDate last, LocalDate today) {
        if (last == null) return StreakOutcome.NO_PREVIOUS;
        if (last.isEqual(today)) return StreakOutcome.SAME_DAY;
        if (last.plusDays(1).isEqual(today)) return StreakOutcome.CONSECUTIVE;
        return StreakOutcome.BROKEN;
    }

    /**
     * Increments the player's streak and rewards them if applicable.
     * @param streak the Streak object representing the player's streak.
     * @param player the Player object representing the player.
     * @param today the current date.
     */
    private void handleStreakIncrement(Streak streak, Player player, LocalDate today) {
        streak.incrementCurrentStreak();
        streak.setLastEarnedDate(today);
        log.debugFmt("Incremented streak %s", streak);
        rewardStreak(streak, player);
    }

    /**
     * Rewards the player based on their current streak.
     * @param streak the Streak object representing the player's streak.
     * @param player the Player object representing the player.
     */
    private void rewardStreak(Streak streak, Player player) {
        int value = streak.getCurrentStreak();
        NavigableMap<Integer, List<String>> rewards = cm.getStreakRewards();

        // start rewards at streaks 2 and above
        if (value < 2) {
            player.sendMessage(
                    Component.text("Earn your /daily-reward tomorrow for a streak reward!")
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.ITALIC));
            return;
        }

        // highest configured tier <= current streak
        Integer floorKey = rewards.floorKey(value);
        if (floorKey == null) {
            return;
        }

        List<String> commands = rewards.get(floorKey);
        if (commands == null || commands.isEmpty()) {
            return;
        }

        String line =
                cm.getOracleLines()
                        .get(ThreadLocalRandom.current().nextInt(cm.getOracleLines().size()));
        player.sendMessage(
                Component.text(String.format(line, floorKey))
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.ITALIC));

        // compute next tier above current streak
        Integer nextKey = rewards.higherKey(value);

        if (nextKey != null) {
            int remaining = nextKey - value;
            player.sendMessage(
                    Component.text(
                                    "Next reward tier at "
                                            + nextKey
                                            + " days ("
                                            + remaining
                                            + " more).")
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.ITALIC));
        } else {
            player.sendMessage(
                    Component.text(
                                    String.format(
                                            "You have reached the Oracle’s highest tier at %d"
                                                    + " days.",
                                            floorKey))
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.ITALIC));
        }
        player.sendMessage(
                Component.text("Your streak is now ", NamedTextColor.YELLOW, TextDecoration.ITALIC)
                        .append(
                                Component.text(
                                        String.valueOf(value),
                                        NamedTextColor.LIGHT_PURPLE,
                                        TextDecoration.ITALIC))
                        .append(
                                Component.text(
                                        " days.", NamedTextColor.YELLOW, TextDecoration.ITALIC)));

        // give the reward for the floor tier
        for (String rewardCommand : commands) {
            String parsedCommand = Util.parseCommand(player, rewardCommand);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            log.info("Sent streak reward of: " + parsedCommand + " to " + player.getName());
        }
    }
}
