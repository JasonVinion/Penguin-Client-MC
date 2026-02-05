package com.penguin.client.module.modules;

import com.penguin.client.mixin.ClientPlayerInteractionManagerAccessor;
import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.GameMode;

public class FastMine extends Module {
    public static FastMine INSTANCE;
    public static ModeSetting mode = new ModeSetting("Mode", "Delay", "Delay", "Speed", "Instant", "Damage", "Creative");
    public static NumberSetting speed = new NumberSetting("Speed", 1.2, 1.0, 5.0, 0.1);

    public FastMine() {
        super("FastMine", "Mines blocks faster. Delay: Removes mining cooldown. Speed: Multiplies mining speed. Instant: Breaks blocks instantly. Damage: Maintains break progress. Creative: Instant break like creative mode.", Category.WORLD);
        addSetting(mode);
        addSetting(speed);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        String currentMode = mode.getMode();
        
        // All modes remove the cooldown for smoother mining
        if (currentMode.equals("Delay") || currentMode.equals("Instant") || 
            currentMode.equals("Damage") || currentMode.equals("Creative")) {
            ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).setBlockBreakingCooldown(0);
        }
    }
    
    /**
     * Get the current speed multiplier for use in mixins
     */
    public static double getSpeedMultiplier() {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            String currentMode = mode.getMode();
            if (currentMode.equals("Speed") || currentMode.equals("Damage")) {
                return speed.getValue();
            }
            if (currentMode.equals("Instant") || currentMode.equals("Creative")) {
                return 100.0; // Effectively instant
            }
        }
        return 1.0;
    }
    
    /**
     * Check if creative-style instant break should be used
     */
    public static boolean isCreativeMode() {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            return mode.getMode().equals("Creative");
        }
        return false;
    }
}
