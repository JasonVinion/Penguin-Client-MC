package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoJump extends Module {
    public AutoJump() {
        super("AutoJump", "Automatically jumps when on ground. Useful for parkour.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
}
