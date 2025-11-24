package com.playtheatria.sessions.listeners;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.events.IncrementRewardCountEvent;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.PLog;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardPlayer implements Listener {
    private final ConfigManager cm;
    private final SessionService sessionService;
    private final StreakService streakService;
    private final PLog log;

    public RewardPlayer(
            ConfigManager configManager,
            SessionService sessionService,
            StreakService streakService,
            PLog log) {
        this.cm = configManager;
        this.sessionService = sessionService;
        this.streakService = streakService;
        this.log = log;
    }

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerUUID());
        if (player == null || !player.isOnline()) {
            log.warn("Player reward returned offline or null");
            log.warn(String.format("Player UUID: %s", event.getPlayerUUID()));
            return;
        }

        switch (sessionService.getSession(event.getPlayerUUID())) {
            case Ok<Session, Exception> ok -> {
                ok.value().setRewarded();
                player.sendMessage(
                        Component.text(cm.getRewardMessage()).color(NamedTextColor.GOLD));
                for (String rewardString : cm.getRewards()) {
                    String parsedCommand = parseCommand(player, rewardString);

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                    log.info("Sent reward of: " + parsedCommand + " to " + player.getName());
                }
                Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
            }
            case Err<Session, Exception> err -> {
                log.err(
                        String.format(
                                "Failed to reward player %s due to session retrieval"
                                        + " error: %s",
                                player.getName(), err.error().getMessage()));
            }
        }

        switch (streakService.getStreak(event.getPlayerUUID())) {
            case Ok<Streak, Exception> ok -> {
                LocalDate today = LocalDate.now(TimeUtils.timeZone);
                try {
                    LocalDate lastDate = ok.value().getLastEarnedDate();
                    if (lastDate == null) {
                        ok.value().incrementCurrentStreak();
                        return;
                    }
                    if (lastDate.isEqual(today)) {
                        return;
                    }
                    if (lastDate.plusDays(1).isEqual(today)) {
                        ok.value().incrementCurrentStreak();
                    } else {
                        ok.value().setCurrentStreakToOne();
                    }
                    ok.value().setLastEarnedDate(today);
                } catch (DateTimeParseException dtpe) {
                    log.err(
                            String.format(
                                    "Failed to parse last earned date for player %s: %s setting"
                                            + " current streak to 1.",
                                    player.getName(), dtpe.getMessage()));
                    ok.value().setCurrentStreakToOne();
                    ok.value().setLastEarnedDate(today);
                }
            }
            case Err<Streak, Exception> err -> log.err(
                    String.format(
                            "Failed to increment streak for player %s due to error: %s",
                            player.getName(), err.error().getMessage()));
        }
    }

    private static String parseCommand(Player player, String rewardString) {
        return rewardString
                .replace("{player}", player.getName())
                .replace("{player_uuid}", player.getUniqueId().toString())
                .replace("{world}", player.getWorld().getName());
    }
}
