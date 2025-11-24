package com.playtheatria.sessions.commands;

import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.Util;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class StreakCommand implements CommandExecutor, TabCompleter {
    private final StreakService streakService;

    public StreakCommand(StreakService streakService) {
        this.streakService = streakService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(Util.PERMISSION_STREAKS_ADMIN)) {
            if (args.length == 1 && args[0].equalsIgnoreCase("show-all")) {
                for (Streak streak : streakService.getStreaks()) {
                    sender.sendMessage(
                            "Player: "
                                    + streak.getPlayerName()
                                    + " | Current Streak: "
                                    + streak.getCurrentStreak()
                                    + " | Longest Streak: "
                                    + streak.getLongestStreak());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission(Util.PERMISSION_STREAKS_ADMIN)) {
            if (args.length == 1) {
                return List.of("show-all");
            }
        }
        return List.of();
    }
}
