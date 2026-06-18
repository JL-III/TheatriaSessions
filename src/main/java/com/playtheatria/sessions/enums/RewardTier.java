package com.playtheatria.sessions.enums;

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

    public static Result<RewardTier, Exception> getByThreshold(int rewardCount) {
        for (RewardTier tier : values()) {
            if (tier.getThreshold() == rewardCount) {
                return new Ok<>(tier);
            }
        }
        return new Err<>(new Exception("No tier found for threshold " + rewardCount));
    }

    public static Result<RewardTier, Exception> getNearestTier(int rewardCount) {
        // values() are in ascending threshold order, so the last tier whose
        // threshold is met is the highest (nearest) one reached.
        Result<RewardTier, Exception> nearest = new Err<>(new Exception("No nearest tier found"));
        for (RewardTier tier : values()) {
            if (tier.getThreshold() <= rewardCount) {
                nearest = new Ok<>(tier);
            }
        }
        return nearest;
    }

    public String getPercentage() {
        return percentage;
    }

    /**
     * Returns the next tier to be unlocked for the given reward count -- the first
     * tier whose threshold is not yet met. Err if every tier has been reached.
     */
    public static Result<RewardTier, Exception> getNextTier(int rewardCount) {
        for (RewardTier tier : values()) {
            if (tier.getThreshold() > rewardCount) {
                return new Ok<>(tier);
            }
        }
        return new Err<>(new Exception("No next tier found"));
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
