package com.playtheatria.theatriaSessions.commands;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.ServerSession;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CommunityCommand implements CommandExecutor, TabCompleter {
    private final ServerSessionManager serverSessionManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public CommunityCommand(ServerSessionManager serverSessionManager, CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.serverSessionManager = serverSessionManager;
        this.customLogger = customLogger;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("theatria.sessions.community.command")) return true;
        switch (args.length) {
            case 0 -> {
                ServerSession serverSession = serverSessionManager.getServerSession();
                customLogger.sendFormattedMessage("ServerSession", sender);
                customLogger.sendFormattedMessage(String.format("Date: %s", serverSession.getSessionDate()), sender);
                customLogger.sendFormattedMessage(String.format("RewardsEarned: %s", serverSession.getRewardsEarned()), sender);
                customLogger.sendFormattedMessage(String.format("PlayersJoined: %s", serverSession.getPlayersJoined()), sender);
                return true;
            }
            default -> {return true;}
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }
}
