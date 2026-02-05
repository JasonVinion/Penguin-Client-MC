package com.penguin.client.module.modules;

import com.penguin.client.mixin.MinecraftClientMixin;
import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "Removes the delay between placing blocks for faster building.", Category.MISC);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ((MinecraftClientMixin) mc).setItemUseCooldown(0);
    }
}
