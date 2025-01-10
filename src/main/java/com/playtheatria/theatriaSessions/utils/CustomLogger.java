package com.playtheatria.theatriaSessions.utils;

import com.playtheatria.theatriaSessions.config.ConfigManager;

public class CustomLogger {
    private final ConfigManager configManager;

    public CustomLogger(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendDebug(String message) {
        if (configManager.isDebug()) {
            Util.sendFormattedLog(message);
        }
    }
}
