package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

/**
 * KnockbackPlus module - Performs more knockback when you hit your target.
 * Works by sending a sprint packet on attack to increase knockback dealt.
 * Ported from meteor-rejects, adapted for 1.20.1
 */
public class KnockbackPlus extends Module {
    public static KnockbackPlus INSTANCE;

    public BooleanSetting onlyKillAura = new BooleanSetting("Only KillAura", false);

    public KnockbackPlus() {
        super("KnockbackPlus", "Performs more knockback when you hit your target by sending sprint packets on attack.", Category.COMBAT);
        addSetting(onlyKillAura);
        INSTANCE = this;
    }

    /**
     * Called from mixin when an attack packet is sent
     */
    public static void onAttack() {
        if (INSTANCE == null || !INSTANCE.isEnabled()) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.networkHandler == null) return;

        // Check if we should only apply with KillAura
        if (INSTANCE.onlyKillAura.isEnabled()) {
            KillAura killAura = ModuleManager.INSTANCE.getModule(KillAura.class);
            if (killAura == null || !killAura.isEnabled()) return;
        }

        // Send sprint packet to increase knockback
        mc.player.networkHandler.sendPacket(
            new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING)
        );
    }
}
