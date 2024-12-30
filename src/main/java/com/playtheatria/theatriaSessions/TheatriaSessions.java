package com.playtheatria.theatriaSessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.theatriaSessions.commands.AdminCommand;
import com.playtheatria.theatriaSessions.commands.SessionCommand;
import com.playtheatria.theatriaSessions.listeners.DayChangeListener;
import com.playtheatria.theatriaSessions.listeners.PlayerJoinListener;
import com.playtheatria.theatriaSessions.listeners.RewardPlayerListener;
import com.playtheatria.theatriaSessions.tasks.SessionManager;
import com.playtheatria.theatriaSessions.tasks.SessionTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TheatriaSessions extends JavaPlugin {

    @Override
    public void onEnable() {
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            Bukkit.getConsoleSender().sendMessage("Essentials returned null, shutting down.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        SessionManager sessionManager = new SessionManager();
        SessionTask sessionTask = new SessionTask(sessionManager, essentials);
        sessionTask.runTaskTimer(this, 20, 20);
        Bukkit.getPluginManager().registerEvents(new DayChangeListener(sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new RewardPlayerListener(), this);
        Objects.requireNonNull(getCommand("session")).setExecutor(new SessionCommand(sessionManager));
        Objects.requireNonNull(getCommand("asession")).setExecutor(new AdminCommand(sessionManager));
    }
}
