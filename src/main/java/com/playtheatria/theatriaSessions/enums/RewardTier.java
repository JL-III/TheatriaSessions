package com.playtheatria.theatriaSessions.enums;

public enum RewardTier {
    TIER1(2, "sell.multiplier.community-goal-1", "10%"),
    TIER2(4, "sell.multiplier.community-goal-2", "20%"),
    TIER3(6, "sell.multiplier.community-goal-3", "30%"),
    TIER4(8, "sell.multiplier.community-goal-4", "40%"),
    TIER5(10, "sell.multiplier.community-goal-5", "50%");

    private final int threshold;
    private final String permission;
    private final String percentage;

    RewardTier(int threshold, String permission, String percentage) {
        this.threshold = threshold;
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
}

