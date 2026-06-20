package com.playtheatria.sessions.commands;

import com.playtheatria.sessions.listeners.CommunityBossBar;
import com.playtheatria.sessions.utils.Util;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /daily-reward bossbar <on|off>} -- per-player toggle for the community boost boss bar.
 *
 * <p>The preference is stored on the player (see {@link CommunityBossBar}), so it sticks across
 * relogs. With no argument it reports the current state.
 */
public class BossBarSubCommand implements SubCommand {
    private final CommunityBossBar communityBossBar;

    public BossBarSubCommand(CommunityBossBar communityBossBar) {
        this.communityBossBar = communityBossBar;
    }

    @Override
    public String name() {
        return "bossbar";
    }

    @Override
    public String permission() {
        return Util.PERMISSION_ALLOW;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can toggle the boss bar.");
            return true;
        }

        if (args.length == 0) {
            String state = communityBossBar.isOptedOut(player) ? "off" : "on";
            sender.sendMessage(
                    Util.formatMessage(
                            "bossbar", state + " -- use /daily-reward bossbar <on|off>"));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "on" -> {
                communityBossBar.setOptedOut(player, false);
                sender.sendMessage(Util.formatMessage("bossbar", "Boss bar enabled."));
                return true;
            }
            case "off" -> {
                communityBossBar.setOptedOut(player, true);
                sender.sendMessage(Util.formatMessage("bossbar", "Boss bar hidden."));
                return true;
            }
            default -> {
                sender.sendMessage(
                        Util.formatMessage("bossbar", "Usage: /daily-reward bossbar <on|off>"));
                return true;
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("on", "off");
        }
        return List.of();
    }
}
