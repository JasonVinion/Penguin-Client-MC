package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoWalk extends Module {
    public AutoWalk() {
        super("AutoWalk", "Holds the forward key automatically so you walk without input.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.options.forwardKey.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.options.forwardKey.setPressed(false);
        }
    }
}
