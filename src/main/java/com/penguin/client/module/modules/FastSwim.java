package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class FastSwim extends Module {
    public FastSwim() {
        super("FastSwim", "Increases swimming speed through water.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && (mc.player.isTouchingWater() || mc.player.isInLava())) {
             mc.player.setVelocity(mc.player.getVelocity().multiply(1.1));
        }
    }
}
