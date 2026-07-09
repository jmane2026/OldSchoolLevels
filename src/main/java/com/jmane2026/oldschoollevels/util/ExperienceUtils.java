package com.jmane2026.oldschoollevels.util;

public class ExperienceUtils {
    public static final int MAX_LEVEL = 99;

    public static int getLevelAtExperience(long xp) {
        for (int level = 1; level <= MAX_LEVEL; level++) {
            if (xp < getXpForLevel(level + 1)) {
                return level;
            }
        }
        return MAX_LEVEL;
    }

    public static long getXpForLevel(int level) {
        if (level <= 1) return 0;
        double total = 0;
        for (int i = 1; i < level; i++) {
            total += Math.floor(i + 300.0 * Math.pow(2.0, i / 7.0));
        }
        return (long) Math.floor(total / 4.0);
    }

    public static long getXpToNextLevel(long currentXp) {
        int currentLevel = getLevelAtExperience(currentXp);
        if (currentLevel >= MAX_LEVEL) return 0;
        return getXpForLevel(currentLevel + 1) - currentXp;
    }
}