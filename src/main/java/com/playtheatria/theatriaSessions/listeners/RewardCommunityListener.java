package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.enums.RewardTier;
import com.playtheatria.theatriaSessions.events.RewardCommunityEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardCommunityListener implements Listener {

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(event.getRewardTier().getPermission())) continue;
            player.sendMessage(Util.formatMessage("Alert!", "You received a community reward: " + event.getRewardTier().name()));

            player.sendMessage(Component.text("Sell Hand Bonus Unlocked: ")
                    .color(TextColor.fromHexString(Util.COLOR_TWO))
                    .append(Component.text(event.getRewardTier().getPercentage())
                            .color(TextColor.fromHexString(Util.COLOR_THREE)))
                    .append(Component.text(" on top of any existing bonuses!")
                            .color(TextColor.fromHexString(Util.COLOR_TWO)))
                    .hoverEvent(Component.text("Each day at 0:00 UTC, all community rewards are reset. Be sure to reach as many tiers as possible before the reset!"))
            );

            RewardTier nextTier = RewardTier.getNextTier(event.getRewardTier());
            if (nextTier != null) {
                player.sendMessage(Component.text("Next Reward Tier: ")
                        .color(TextColor.fromHexString(Util.COLOR_TWO))
                        .append(Component.text(nextTier.name())
                                .color(TextColor.fromHexString(Util.COLOR_THREE)))
                        .hoverEvent(Component.text("This is the next tier that can be unlocked when " + nextTier.getThreshold() + " total players earn their /daily-rewards!"))
                        .color(TextColor.fromHexString(Util.COLOR_TWO))
                );

                player.sendMessage(Component.text("Keep playing to unlock more rewards and bonuses!")
                        .color(TextColor.color(NamedTextColor.GREEN))
                        .hoverEvent(Component.text("Everyone who meets their /daily-reward requirement contributes to these buffs! Earn yours and get others to earn theirs as well!"))
                );
            } else {
                player.sendMessage(Component.text("You’ve unlocked the final reward tier! Congratulations!")
                        .color(TextColor.color(NamedTextColor.GOLD))
                        .hoverEvent(Component.text("No more rewards are available for today, but you can still enjoy the unlocked bonuses."))
                );
            }

        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp group default permission settemp " + event.getRewardTier().getPermission() + " true 1day");
//        Util.sendFormattedLog("Sent community reward of: " + event.getRewardCommand() + " for reaching threshold of " + event.getThreshold());
    }
}
