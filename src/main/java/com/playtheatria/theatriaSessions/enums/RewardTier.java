package com.playtheatria.theatriaSessions.enums;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;

public enum RewardTier {
    TIER1(4, "Tier 1", "sell.multiplier.community-goal-1", "10%"),
    TIER2(6, "Tier 2", "sell.multiplier.community-goal-2", "20%"),
    TIER3(8, "Tier 3", "sell.multiplier.community-goal-3", "30%"),
    TIER4(10, "Tier 4", "sell.multiplier.community-goal-4", "40%"),
    TIER5(12, "Tier 5", "sell.multiplier.community-goal-5", "50%");

    private final int threshold;
    private final String displayName;
    private final String permission;
    private final String percentage;

    RewardTier(int threshold, String displayName, String permission, String percentage) {
        this.threshold = threshold;
        this.displayName = displayName;
        this.permission = permission;
        this.percentage = percentage;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getPermission() {
        return permission;
    }

    public static RewardTier getByThreshold(int rewardCount) {
        for (RewardTier tier : values()) {
            if (tier.getThreshold() == rewardCount) {
                return tier;
            }
        }
        return null; // No matching tier
    }

    public static Result<RewardTier, Exception> getNearestTier(int rewardCount) {
        RewardTier nearestTier = null;

        for (RewardTier tier : values()) {
            if (tier.getThreshold() <= rewardCount) {
                // Update nearest tier if the current one is closer
                if (nearestTier == null || tier.getThreshold() > nearestTier.getThreshold()) {
                    nearestTier = tier;
                }
            }
        }
        if (nearestTier == null) {
            return new Err<>(new Exception("No nearest tier found"));
        } else {
            return new Ok<>(nearestTier);
        }
    }

    public String getPercentage() {
        return percentage;
    }

    public static RewardTier getNextTier(RewardTier currentTier) {
        RewardTier[] tiers = RewardTier.values();
        int currentIndex = currentTier.ordinal();

        if (currentIndex + 1 < tiers.length) {
            return tiers[currentIndex + 1];
        }
        return null; // No next tier exists
    }

    public String getDisplayName() {
        return displayName;
    }
}
