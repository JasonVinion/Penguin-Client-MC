package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;

public class Sprint extends Module {

    private static Sprint INSTANCE;
    
    private BooleanSetting omnidirectional = new BooleanSetting("Omnidirectional", false);
    private BooleanSetting keepSprint = new BooleanSetting("Keep Sprint", true);
    private ModeSetting mode = new ModeSetting("Mode", "Rage", "Rage", "Legit");

    public Sprint() {
        super("Sprint", "Keeps sprint enabled automatically while moving forward.", Category.MOVEMENT);
        addSetting(mode);
        addSetting(omnidirectional);
        addSetting(keepSprint);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        boolean shouldSprint = false;
        
        if (mode.getMode().equals("Rage")) {
            // Always sprint if moving
            if (omnidirectional.isEnabled()) {
                shouldSprint = mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
            } else {
                shouldSprint = mc.player.forwardSpeed > 0;
            }
        } else {
            // Legit mode - only sprint forward when not colliding
            shouldSprint = mc.player.forwardSpeed > 0 && !mc.player.horizontalCollision;
        }
        
        if (shouldSprint && !mc.player.isSneaking() && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
    
    public static boolean shouldKeepSprint() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.keepSprint.isEnabled();
    }
}
