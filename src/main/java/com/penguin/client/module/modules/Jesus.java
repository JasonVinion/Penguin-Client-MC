package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;

public class Jesus extends Module {
    
    private ModeSetting mode = new ModeSetting("Mode", "Solid", "Solid", "Dolphin");
    private BooleanSetting water = new BooleanSetting("Water", true);
    private BooleanSetting lava = new BooleanSetting("Lava", true);
    
    private static Jesus INSTANCE;

    public Jesus() {
        super("Jesus", "Walk on water and lava by making them solid.", Category.MOVEMENT);
        addSetting(mode);
        addSetting(water);
        addSetting(lava);
        INSTANCE = this;
    }
    
    public static boolean isEnabledStatic() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
    
    public static boolean shouldSolidifyWater() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.water.isEnabled();
    }
    
    public static boolean shouldSolidifyLava() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.lava.isEnabled();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        boolean inWater = mc.player.isTouchingWater() && water.isEnabled();
        boolean inLava = mc.player.isInLava() && lava.isEnabled();
        
        if (inWater || inLava) {
            if (mc.options.jumpKey.isPressed() || mc.player.isSneaking()) return;

            if (mode.getMode().equals("Solid")) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0, mc.player.getVelocity().z);
                mc.player.setOnGround(true);
            } else if (mode.getMode().equals("Dolphin")) {
                // Dolphin mode - swim faster
                mc.player.setVelocity(
                    mc.player.getVelocity().x * 1.3,
                    0.1,
                    mc.player.getVelocity().z * 1.3
                );
            }
        }
    }
}
