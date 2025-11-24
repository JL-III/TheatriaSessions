package com.playtheatria.sessions.commands;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.Util;
import java.time.LocalDate;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class StreakCommand implements CommandExecutor, TabCompleter {
    private final StreakService streakService;

    public StreakCommand(StreakService streakService) {
        this.streakService = streakService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(Util.PERMISSION_STREAKS_ADMIN)) {
            switch (args.length) {
                case 0 -> {
                    sender.sendMessage("Usage: /streak <show-all> - Show all player streaks.");
                    return true;
                }
                case 1 -> {
                    switch (args[0].toLowerCase()) {
                            // TODO create a reset command for testing
                        case "force-streak" -> {
                            if (sender instanceof Player player) {
                                switch (streakService.getStreak(player.getUniqueId())) {
                                    case Ok<Streak, Exception> ok -> {
                                        // Set last earned date to yesterday to force rewarding
                                        ok.value()
                                                .setLastEarnedDate(
                                                        LocalDate.now(TimeUtils.timeZone)
                                                                .minusDays(1));
                                        streakService.handleStreak(player.getUniqueId(), player);
                                        sender.sendMessage(
                                                "Forced incremented streak for player "
                                                        + player.getName()
                                                        + " to "
                                                        + ok.value().getCurrentStreak());
                                        return true;
                                    }
                                    case Err<Streak, Exception> err -> {
                                        sender.sendMessage(
                                                "Failed to force increment streak for player "
                                                        + player.getName()
                                                        + ": "
                                                        + err.error().getMessage());
                                        return true;
                                    }
                                }
                            } else {
                                sender.sendMessage(
                                        "Only players can use the force-streak command.");
                                return true;
                            }
                        }
                        case "reset" -> {
                            if (sender instanceof Player player) {
                                Streak streak = new Streak(player.getUniqueId(), player.getName());
                                streakService.setStreak(streak);
                                sender.sendMessage("Reset streak %s ".formatted(streak));
                                return true;
                            } else {
                                sender.sendMessage("Only players can use the reset command.");
                                return true;
                            }
                        }
                        case "show-all" -> {
                            for (Streak streak : streakService.getStreaks()) {
                                sender.sendMessage(
                                        "Player: "
                                                + streak.getPlayerName()
                                                + " | Current Streak: "
                                                + streak.getCurrentStreak()
                                                + " | Longest Streak: "
                                                + streak.getLongestStreak()
                                                + " | Last Earned: "
                                                + streak.getLastEarnedDate());
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(Util.PERMISSION_STREAKS_ADMIN)) {
            if (args.length == 1) {
                return List.of("force-streak", "reset", "show-all");
            }
        }
        return List.of();
    }
}
