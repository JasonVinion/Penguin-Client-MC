package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * PortalGodMode - Go into god mode (creative-like) when entering a portal
 */
public class PortalGodMode extends Module { // deprecated
    public static PortalGodMode INSTANCE;
    private boolean wasInPortal = false;

    public PortalGodMode() {
        super("PortalGodMode", "Enables god mode when you enter a portal.", Category.PLAYER);
        INSTANCE = this;
        setVisible(false);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Check if player is in a nether portal
        boolean inPortal = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == net.minecraft.block.Blocks.NETHER_PORTAL;
        
        if (inPortal && !wasInPortal) {
            // Entering portal - enable god mode
            mc.player.getAbilities().invulnerable = true;
            mc.player.sendAbilitiesUpdate();
            wasInPortal = true;
        } else if (!inPortal && wasInPortal) {
            // Leaving portal - disable god mode
            mc.player.getAbilities().invulnerable = false;
            mc.player.sendAbilitiesUpdate();
            wasInPortal = false;
        }
    }
    
    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.getAbilities().invulnerable = false;
            mc.player.sendAbilitiesUpdate();
        }
        wasInPortal = false;
    }
}
