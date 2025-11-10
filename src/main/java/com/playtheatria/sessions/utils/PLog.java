package com.playtheatria.sessions.utils;

import com.playtheatria.sessions.config.ConfigManager;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public final class PLog {
    private final ConfigManager configManager;

    public PLog(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void info(String msg) {
        Bukkit.getConsoleSender().sendMessage(Util.formatMessage("TheatriaSessions", msg));
    }

    public void warn(String msg) {
        Bukkit.getLogger().log(Level.WARNING, "[TheatriaSessions] {0}", msg);
    }

    public void err(String msg) {
        Bukkit.getLogger().log(Level.SEVERE, "[TheatriaSessions] {0}", msg);
    }

    /**
     * Plugin debug logging method.
     * @param msg The debug message to log.
     */
    public void debug(String msg) {
        if (!configManager.isDebug()) {
            return;
        }
        Bukkit.getConsoleSender().sendMessage(Util.formatMessage("TheatriaSessions", msg));
    }
}
