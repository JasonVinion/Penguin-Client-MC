package com.penguin.client.module.modules;

import com.penguin.client.mixin.MinecraftClientMixin;
import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public class FastExp extends Module {
    public FastExp() {
        super("FastExp", "Removes delay when throwing experience bottles for faster mending.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE) {
             ((MinecraftClientMixin) mc).setItemUseCooldown(0);
        }
    }
}
