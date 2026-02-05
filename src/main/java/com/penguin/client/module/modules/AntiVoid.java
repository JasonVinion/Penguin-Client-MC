package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AntiVoid extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Bounce", "Bounce", "Reposition", "Packet");
    private Vec3d lastGroundPos;

    public AntiVoid() {
        super("AntiVoid", "Teleports you back up when about to fall into the void.", Category.MOVEMENT);
        addSetting(mode);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isOnGround()) {
            lastGroundPos = mc.player.getPos();
        }

        if (mc.player.fallDistance > 3.0f && !mc.player.getAbilities().flying) {
            boolean voidBelow = true;
            int minY = mc.world.getBottomY();

            for (int y = (int) mc.player.getY(); y >= minY; y--) {
                BlockPos pos = new BlockPos((int)mc.player.getX(), y, (int)mc.player.getZ());
                if (!mc.world.isAir(pos)) {
                    voidBelow = false;
                    break;
                }
            }

            if (voidBelow) {
                 if (mode.getMode().equals("Bounce")) {
                     Vec3d vel = mc.player.getVelocity();
                     mc.player.setVelocity(vel.x, 0.5, vel.z); // Bounce up
                     mc.player.fallDistance = 0;
                 } else if (mode.getMode().equals("Reposition")) {
                     if (lastGroundPos != null) {
                         double targetY = lastGroundPos.y + 1;
                         // Validate target position is within world height limits
                         if (targetY >= mc.world.getBottomY() && targetY <= mc.world.getTopY()) {
                             mc.player.setPosition(lastGroundPos.x, targetY, lastGroundPos.z);
                             mc.player.setVelocity(0, 0, 0);
                             mc.player.fallDistance = 0;
                         }
                     }
                 } else if (mode.getMode().equals("Packet")) {
                     if (lastGroundPos != null) {
                         double targetY = lastGroundPos.y + 1;
                         // Validate target position is within world height limits
                         if (targetY >= mc.world.getBottomY() && targetY <= mc.world.getTopY()) {
                             // Send packet to trick server
                             mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(lastGroundPos.x, targetY, lastGroundPos.z, true));
                             // Also set client side to avoid glitchy visual
                             mc.player.setVelocity(0, 0, 0);
                         }
                     }
                 }
            }
        }
    }
}
