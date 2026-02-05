package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.MovementUtils;
import net.minecraft.client.MinecraftClient;

public class Strafe extends Module {
    private NumberSetting speed = new NumberSetting("Speed", 0.2, 0.1, 2.0, 0.1);

    public Strafe() {
        super("Strafe", "Improves air control while strafing for better movement.", Category.MOVEMENT);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (MovementUtils.isMoving()) {
            if (mc.player.isOnGround()) return;

            double direction = MovementUtils.getDirection();
            double currentSpeed = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
            double baseSpeed = speed.getValue();

            if (currentSpeed < baseSpeed) currentSpeed = baseSpeed;

            double x = -Math.sin(direction) * currentSpeed;
            double z = Math.cos(direction) * currentSpeed;

            mc.player.setVelocity(x, mc.player.getVelocity().y, z);
        }
    }
}
