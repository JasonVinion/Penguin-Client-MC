package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class SprintReset extends Module {
    public static SprintReset INSTANCE;
    
    private NumberSetting resetDelay = new NumberSetting("Reset Delay", 1.0, 0.0, 5.0, 1.0);
    
    private static boolean pendingReset = false;
    private static int resetTimer = 0;

    public SprintReset() {
        super("SprintReset", "Re-enables sprint after attacking. Briefly stops sprint before attack for extra knockback.", Category.COMBAT);
        addSetting(resetDelay);
        INSTANCE = this;
    }
    
    @Override
    public void onTick() {
        if (!isEnabled()) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        // Handle pending sprint reset
        if (pendingReset) {
            if (resetTimer > 0) {
                resetTimer--;
            } else {
                // Re-enable sprint and notify server
                mc.player.setSprinting(true);
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                pendingReset = false;
            }
        }
    }

    public static void onAttack() {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.player.isSprinting()) {
                // Stop sprinting and immediately notify server before the attack packet is sent
                mc.player.setSprinting(false);
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                // Schedule sprint to be re-enabled after delay
                pendingReset = true;
                resetTimer = (int) INSTANCE.resetDelay.getValue();
            }
        }
    }
}
