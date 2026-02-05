package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Instantly clicks respawn button when you die.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.isDead()) {
            mc.player.requestRespawn();
        }
    }
}
