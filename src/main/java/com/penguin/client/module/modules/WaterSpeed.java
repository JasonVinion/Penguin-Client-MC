package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * WaterSpeed - Move faster in water and optionally lava
 */
public class WaterSpeed extends Module {
    public static WaterSpeed INSTANCE;
    
    private NumberSetting speed = new NumberSetting("Speed", 2.0, 1.0, 5.0, 0.1);
    private BooleanSetting lava = new BooleanSetting("Lava", true);

    public WaterSpeed() {
        super("WaterSpeed", "Move faster in water and lava.", Category.MOVEMENT);
        addSetting(speed);
        addSetting(lava);
        INSTANCE = this;
    }
    
    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        if (mc.player.isTouchingWater() || (lava.isEnabled() && mc.player.isInLava())) {
            // Get current velocity and add a small boost rather than multiply
            Vec3d vel = mc.player.getVelocity();
            double boost = (speed.getValue() - 1.0) * 0.1;
            
            // Only boost horizontal movement when actually moving (higher threshold)
            if (Math.abs(vel.x) > 0.03 || Math.abs(vel.z) > 0.03) {
                mc.player.setVelocity(
                    vel.x + (vel.x * boost),
                    vel.y,
                    vel.z + (vel.z * boost)
                );
            }
        }
    }
}
