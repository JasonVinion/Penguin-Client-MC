package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class Spider extends Module {
    public Spider() {
        super("Spider", "Allows you to climb up walls like a spider by holding jump near walls.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.horizontalCollision) {
            Vec3d velocity = mc.player.getVelocity();
            if (velocity.y < 0.2) {
                mc.player.setVelocity(velocity.x, 0.2, velocity.z);
            }
        }
    }
}
