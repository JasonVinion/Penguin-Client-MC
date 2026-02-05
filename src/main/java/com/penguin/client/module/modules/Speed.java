package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.MovementUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public class Speed extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Strafe", "Strafe", "Ground");
    private NumberSetting speed = new NumberSetting("Speed", 2.0, 0.1, 10.0, 0.1);

    public Speed() {
        super("Speed", "Increases movement speed. Strafe mode bunny hops, Ground mode stays grounded.", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Entity entity = mc.player.getVehicle() != null ? mc.player.getVehicle() : mc.player;

        if (MovementUtils.isMoving()) {
            if (mode.getMode().equals("Strafe")) {
                if (mc.player.isOnGround() && !mc.player.isClimbing()) {
                    mc.player.jump();
                }
            }

            double direction = MovementUtils.getDirection();
            double s = speed.getValue() * 0.2;

            double x = -Math.sin(direction) * s;
            double z = Math.cos(direction) * s;

            entity.setVelocity(x, entity.getVelocity().y, z);
        } else {
            entity.setVelocity(0, entity.getVelocity().y, 0);
        }
    }
}
