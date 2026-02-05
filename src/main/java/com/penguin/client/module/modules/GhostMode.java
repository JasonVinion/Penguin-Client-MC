package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;

/**
 * GhostMode module - Allows you to keep playing after you die.
 * Works on Forge, Fabric and Vanilla servers.
 * Ported from meteor-rejects, adapted for 1.20.1
 */
public class GhostMode extends Module { // deprecated
    public static GhostMode INSTANCE;

    public BooleanSetting fullFood = new BooleanSetting("Full Food", true);
    
    private boolean active = false;

    public GhostMode() {
        super("GhostMode", "Allows you to keep playing after you die. Works on Forge, Fabric and Vanilla servers. Blocks death screen and restores health/hunger.", Category.PLAYER);
        addSetting(fullFood);
        INSTANCE = this;
        setVisible(false);
    }

    @Override
    public void onDisable() {
        active = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.requestRespawn();
        }
    }

    @Override
    public void onTick() {
        if (!active) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        if (mc.player.getHealth() < 1f) {
            mc.player.setHealth(20f);
        }
        
        if (fullFood.isEnabled() && mc.player.getHungerManager().getFoodLevel() < 20) {
            mc.player.getHungerManager().setFoodLevel(20);
        }
    }

    /**
     * Called from mixin when death screen would open
     */
    public void onDeathScreen() {
        if (!active) {
            active = true;
        }
    }

    public boolean isGhostActive() {
        return active && isEnabled();
    }
}
