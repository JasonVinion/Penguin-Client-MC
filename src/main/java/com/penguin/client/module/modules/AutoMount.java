package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.Hand;

public class AutoMount extends Module {
    public AutoMount() {
        super("AutoMount", "Automatically mounts rideable entities like horses and boats when nearby.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.hasVehicle()) return;

        for (Entity e : mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(3))) {
            if (e.distanceTo(mc.player) < 3) {
                if (e instanceof HorseEntity || e instanceof BoatEntity || e instanceof MinecartEntity) {
                    mc.interactionManager.interactEntity(mc.player, e, Hand.MAIN_HAND);
                    return;
                }
            }
        }
    }
}
