package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module {
    public static NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
    public static Vec3d pos = Vec3d.ZERO;
    public static float yaw, pitch;
    public static boolean active = false;

    public Freecam() {
        super("Freecam", "Detaches camera from player allowing free camera movement.", Category.RENDER);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            pos = mc.gameRenderer.getCamera().getPos();
            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
            active = true;
        }
    }

    @Override
    public void onDisable() {
        active = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
             mc.player.setVelocity(0,0,0);
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (active && mc.player != null) {
            mc.player.setVelocity(0, 0, 0);
            mc.player.input.movementForward = 0;
            mc.player.input.movementSideways = 0;
            mc.player.setJumping(false);
            mc.player.setSneaking(false);

            float s = (float) speed.getValue();

            float forward = 0;
            float strafe = 0;
            float vertical = 0;

            if (mc.options.forwardKey.isPressed()) forward++;
            if (mc.options.backKey.isPressed()) forward--;
            if (mc.options.leftKey.isPressed()) strafe++;
            if (mc.options.rightKey.isPressed()) strafe--;
            if (mc.options.jumpKey.isPressed()) vertical++;
            if (mc.options.sneakKey.isPressed()) vertical--;

            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();

            double rad = Math.toRadians(yaw + 90);
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);

            double x = (forward * cos + strafe * sin) * s;
            double z = (forward * sin - strafe * cos) * s;

            pos = pos.add(x, vertical * s, z);
        }
    }
}
