package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class Parkour extends Module {
    public Parkour() {
        super("Parkour", "Automatically jumps when you reach the edge of a block.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.isOnGround() && !mc.player.isSneaking()) {
             if (mc.world.getBlockState(mc.player.getBlockPos().down().offset(mc.player.getHorizontalFacing())).isAir()) {
                 // Nothing in front, but are we on edge?
                 // Simple logic: if standing on edge, jump.
                 if (!mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(mc.player.getVelocity().x, -0.5, mc.player.getVelocity().z)).iterator().hasNext()) {
                     mc.player.jump();
                 }
             }
        }
    }
}
