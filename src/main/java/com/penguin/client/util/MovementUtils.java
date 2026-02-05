package com.penguin.client.util;

import net.minecraft.client.MinecraftClient;

public class MovementUtils {

    public static double getDirection() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return 0;

        float rotationYaw = mc.player.getYaw();
        float forward = 1f;

        if (mc.player.input.movementForward < 0) {
            rotationYaw += 180;
            forward = -0.5f;
        } else if (mc.player.input.movementForward > 0) {
            forward = 0.5f;
        }

        if (mc.player.input.movementSideways > 0) {
            rotationYaw -= 90 * forward;
        } else if (mc.player.input.movementSideways < 0) {
            rotationYaw += 90 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static boolean isMoving() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }
}
