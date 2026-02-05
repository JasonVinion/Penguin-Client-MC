package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;

/**
 * AntiCrash module - Attempts to cancel packets that may crash the client.
 * Ported from meteor-rejects, adapted for 1.20.1
 */
public class AntiCrash extends Module {
    public static AntiCrash INSTANCE;

    public BooleanSetting logAttempts = new BooleanSetting("Log Attempts", false);

    public AntiCrash() {
        super("AntiCrash", "Attempts to cancel packets that may crash the client. Blocks explosions, particles, and movement packets with invalid values.", Category.MISC);
        addSetting(logAttempts);
        INSTANCE = this;
    }

    /**
     * Check if a position is within valid world bounds
     */
    public static boolean isPositionValid(double x, double y, double z) {
        return Math.abs(x) <= 30_000_000 && Math.abs(y) <= 30_000_000 && Math.abs(z) <= 30_000_000;
    }

    /**
     * Check if a velocity is within valid bounds
     */
    public static boolean isVelocityValid(double x, double y, double z) {
        return Math.abs(x) <= 30_000_000 && Math.abs(y) <= 30_000_000 && Math.abs(z) <= 30_000_000;
    }

    /**
     * Check if particle count is within valid bounds
     */
    public static boolean isParticleCountValid(int count) {
        return count <= 100_000;
    }
}
