package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

/**
 * Control any rideable entity with enhanced settings.
 * Allows riding without a saddle and speed modifications.
 */
public class EntityControl extends Module {
    public static EntityControl INSTANCE;
    
    private BooleanSetting noSaddle = new BooleanSetting("No Saddle", true);
    private BooleanSetting noWaterSlow = new BooleanSetting("No Water Slow", true);
    private NumberSetting speedBoost = new NumberSetting("Speed Boost", 1.0, 1.0, 3.0, 0.1);
    private BooleanSetting controlInAir = new BooleanSetting("Control In Air", true);
    private BooleanSetting antiKick = new BooleanSetting("Anti-Dismount", true);

    public EntityControl() {
        super("EntityControl", "Control any rideable entity. No Saddle allows riding horses without saddles.", Category.MOVEMENT);
        addSetting(noSaddle);
        addSetting(noWaterSlow);
        addSetting(speedBoost);
        addSetting(controlInAir);
        addSetting(antiKick);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;
        
        // Apply speed boost if enabled
        if (speedBoost.getValue() > 1.0) {
            double boost = speedBoost.getValue();
            vehicle.setVelocity(
                vehicle.getVelocity().x * boost,
                vehicle.getVelocity().y,
                vehicle.getVelocity().z * boost
            );
        }
    }

    /**
     * Check if the player can control an entity without a saddle.
     * Called from mixin.
     */
    public static boolean canControlWithoutSaddle() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.noSaddle.isEnabled();
    }
    
    /**
     * Check if water slowdown should be disabled.
     */
    public static boolean shouldDisableWaterSlow() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.noWaterSlow.isEnabled();
    }
    
    /**
     * Check if player should be able to control entity while in air.
     */
    public static boolean canControlInAir() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.controlInAir.isEnabled();
    }
    
    /**
     * Check if anti-dismount should be active.
     */
    public static boolean shouldPreventDismount() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.antiKick.isEnabled();
    }
}
