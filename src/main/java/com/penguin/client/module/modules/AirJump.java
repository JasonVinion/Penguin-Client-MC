package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class AirJump extends Module {
    public AirJump() {
        super("AirJump", "Allows you to jump multiple times in the air.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (mc.options.jumpKey.isPressed() && !mc.player.isOnGround()) {
             // Trick client into jumping by setting onGround temporarily
             mc.player.setOnGround(true);
             mc.player.jump();
             // Reset to actual state after jump
             mc.player.setOnGround(false);
        }
    }
}
