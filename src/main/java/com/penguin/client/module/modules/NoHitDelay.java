package com.penguin.client.module.modules;

import com.penguin.client.mixin.MinecraftClientMixin;
import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class NoHitDelay extends Module {
    public NoHitDelay() {
        super("NoHitDelay", "Removes the hit delay.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        // Since MinecraftClientMixin is an interface on the client instance
        ((MinecraftClientMixin) mc).setItemUseCooldown(0);
    }
}
