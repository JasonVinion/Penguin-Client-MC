package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/**
 * ElytraTweaks - Multiple tweaks for elytra flying
 */
public class ElytraTweaks extends Module {
    public static ElytraTweaks INSTANCE;
    
    private BooleanSetting fixRocketDesync = new BooleanSetting("Fix Rocket Desync", true);
    private BooleanSetting noBounce = new BooleanSetting("No Bounce", true);
    private BooleanSetting rocketAcceleration = new BooleanSetting("Rocket Acceleration", true);
    private NumberSetting accelerationMultiplier = new NumberSetting("Acceleration", 1.5, 1.0, 3.0, 0.1);
    private BooleanSetting chestplateSwap = new BooleanSetting("Chestplate Swap", false);
    private BooleanSetting extendRockets = new BooleanSetting("Extend Rockets", false);
    private NumberSetting rocketDuration = new NumberSetting("Rocket Duration", 2.0, 1.0, 5.0, 0.5);

    public ElytraTweaks() {
        super("ElytraTweaks", "Multiple tweaks for elytra flying: desync fix, no bounce, acceleration, chestplate swap, extended rockets.", Category.MOVEMENT);
        addSetting(fixRocketDesync);
        addSetting(noBounce);
        addSetting(rocketAcceleration);
        addSetting(accelerationMultiplier);
        addSetting(chestplateSwap);
        addSetting(extendRockets);
        addSetting(rocketDuration);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // No Bounce - prevent bouncing when landing with elytra
        // Check for low Y velocity while flying close to ground to prevent bounce
        if (noBounce.isEnabled() && mc.player.isFallFlying()) {
            Vec3d velocity = mc.player.getVelocity();
            // If falling fast and very close to ground, reduce vertical velocity
            if (velocity.y < -0.5 && mc.player.getY() - Math.floor(mc.player.getY()) < 0.1) {
                mc.player.setVelocity(velocity.x, Math.max(velocity.y, -0.1), velocity.z);
            }
        }

        // Rocket Acceleration - add velocity boost when using rockets (not multiplicative)
        if (rocketAcceleration.isEnabled() && mc.player.isFallFlying()) {
            Vec3d velocity = mc.player.getVelocity();
            double speed = velocity.length();
            
            // Only boost if moving and below a reasonable max speed
            if (speed > 0.1 && speed < 2.0) {
                Vec3d direction = velocity.normalize();
                double boost = (accelerationMultiplier.getValue() - 1.0) * 0.05;
                mc.player.setVelocity(velocity.add(direction.multiply(boost)));
            }
        }

        // Chestplate Swap - server thinks you're wearing elytra even with chestplate
        // This is handled by mixin to send fake equipment updates
    }
    
    public static boolean shouldFixRocketDesync() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.fixRocketDesync.isEnabled();
    }
    
    public static boolean shouldPreventBounce() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.noBounce.isEnabled();
    }
    
    public static boolean shouldSwapChestplate() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.chestplateSwap.isEnabled();
    }
    
    public static boolean shouldExtendRockets() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.extendRockets.isEnabled();
    }
    
    public static double getRocketDuration() {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            return INSTANCE.rocketDuration.getValue();
        }
        return 1.0;
    }
}
