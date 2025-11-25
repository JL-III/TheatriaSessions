package com.playtheatria.sessions.utils;

import com.playtheatria.sessions.config.ConfigManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PLog {
    private final ConfigManager configManager;
    private static final Logger logger =
            Logger.getLogger("TheatriaSessions " + PLog.class.getSimpleName());

    public PLog(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void info(String msg) {
        logger.log(Level.INFO, msg);
    }

    public void warn(String msg) {
        logger.log(Level.WARNING, "[TheatriaSessions] %s", msg);
    }

    public void err(String msg) {
        logger.log(Level.SEVERE, "[TheatriaSessions] %s", msg);
    }

    public void errFmt(String template, Object arg) {
        logger.log(Level.SEVERE, String.format(template, arg));
    }

    /**
     * Plugin debug logging method.
     * @param msg The debug message to log.
     */
    public void debug(String msg) {
        if (!configManager.isDebug()) {
            return;
        }
        logger.log(Level.INFO, msg);
    }

    public void debugFmt(String template, Object arg) {
        if (!configManager.isDebug()) {
            return;
        }
        logger.log(Level.INFO, String.format(template, arg));
    }

    public void debugFmt(String template, Object[] arg) {
        if (!configManager.isDebug()) {
            return;
        }
        logger.log(Level.INFO, String.format(template, arg));
    }
}
