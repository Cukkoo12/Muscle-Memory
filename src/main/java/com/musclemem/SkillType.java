package com.musclemem;

public enum SkillType {
    BLOCK_BREAK("Block Breaking", 500, 2000, 5000),
    BLOCK_PLACE("Block Placing", 300, 1500, 4000),
    MELEE_HIT("Melee Combat", 200, 800, 2000),
    BOW_SHOOT("Archery", 50, 200, 500),
    SWIMMING("Swimming", 100, 500, 1500),
    JUMPING("Jumping", 500, 2000, 5000),
    FISHING("Fishing", 20, 75, 200),
    MOB_KILL("Mob Slaying", 50, 200, 500),
    SLEEPING("Sleeping", 10, 30, 75),
    SPRINTING("Sprinting", 1000, 5000, 15000);

    private final String displayName;
    private final int[] thresholds;

    SkillType(String displayName, int level1, int level2, int level3) {
        this.displayName = displayName;
        this.thresholds = new int[]{level1, level2, level3};
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getThreshold(int level) {
        if (level < 1 || level > 3) return Integer.MAX_VALUE;
        return thresholds[level - 1];
    }

    public int getLevel(int count) {
        if (count >= thresholds[2]) return 3;
        if (count >= thresholds[1]) return 2;
        if (count >= thresholds[0]) return 1;
        return 0;
    }

    public int getNextThreshold(int count) {
        int level = getLevel(count);
        if (level >= 3) return thresholds[2];
        return thresholds[level];
    }

    public static SkillType fromString(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
