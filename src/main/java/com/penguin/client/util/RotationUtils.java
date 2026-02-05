package com.penguin.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {

    public static float[] getRotations(Entity target) {
        if (target == null) return null;
        return getRotations(target.getPos().add(0, target.getEyeHeight(target.getPose()), 0));
    }

    public static float[] getRotations(Vec3d targetPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return null;

        Vec3d eyesPos = mc.player.getEyePos();
        double diffX = targetPos.x - eyesPos.x;
        double diffY = targetPos.y - eyesPos.y;
        double diffZ = targetPos.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]{
            MathHelper.wrapDegrees(yaw),
            MathHelper.wrapDegrees(pitch)
        };
    }
}
