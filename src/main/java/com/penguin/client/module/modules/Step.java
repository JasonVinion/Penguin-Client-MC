package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

public class Step extends Module {

    private NumberSetting height = new NumberSetting("Height", 1.0, 0.6, 2.5, 0.1);

    public Step() {
        super("Step", "Allows stepping up blocks without jumping. Configurable height.", Category.MOVEMENT);
        addSetting(height);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.setStepHeight((float) height.getValue());
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.setStepHeight(0.6f);
        }
    }
}
