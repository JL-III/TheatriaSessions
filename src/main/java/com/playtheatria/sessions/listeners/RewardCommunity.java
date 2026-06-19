package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.RewardCommunityEvent;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardCommunity implements Listener {
    // Oracle styling matched to the sell-multiplier "Boon" message: a green gradient
    // body with a yellow highlight on the key terms (same hexes, so the two read alike).
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final String GRADIENT = "<gradient:#67e9a8:#1fa86b>";
    private static final String HL = "<color:#ffd119>";
    private static final String HL_END = "</color>";

    private final ConfigManager configManager;
    private final PLog log;

    public RewardCommunity(ConfigManager configManager, PLog log) {
        this.log = log;
        this.configManager = configManager;
    }

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        if (!configManager.isCommunityRewardsEnabled()) {
            log.debug("Reward Community Event fired!");
            return;
        }

        RewardTier tier = event.getRewardTier();

        // Built once and shared (Adventure Components are immutable) -- same message for all.
        Component unlock =
                MINI.deserialize(
                        GRADIENT
                                + "The Oracle rewards the realm! "
                                + HL
                                + tier.getDisplayName()
                                + HL_END
                                + " unlocked ("
                                + HL
                                + "+"
                                + tier.getPercentage()
                                + HL_END
                                + " sell hand) for everyone until reset!");
        Component progress =
                MINI.deserialize(
                                GRADIENT
                                        + "Check your progress with "
                                        + HL
                                        + "/daily-reward"
                                        + HL_END)
                        .clickEvent(ClickEvent.runCommand("/daily-reward view community"))
                        .hoverEvent(HoverEvent.showText(Component.text("Open the community tab")));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(tier.getPermission())) continue;
            player.sendMessage(unlock);
            player.sendMessage(progress);
        }

        boolean granted =
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        Util.grantCommunityPermCommand(
                                tier.getPermission(),
                                configManager.getCommunityBonusGroup(),
                                configManager.getCommunityBonusDuration()));
        if (!granted) {
            log.warn(
                    "Failed to grant community permission '"
                            + tier.getPermission()
                            + "'. Is LuckPerms installed?");
        }
    }
}
