package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.MovementUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Phase extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Clip", "Clip", "Packet", "Sand");
    private NumberSetting distance = new NumberSetting("Distance", 0.5, 0.0, 5.0, 0.1);

    public Phase() {
        super("Phase", "Allows you to phase through blocks. Works best with thin blocks like fences, doors, and walls.", Category.MOVEMENT);
        addSetting(mode);
        addSetting(distance);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Trigger phase only when colliding horizontally with a block
        if (mc.player.horizontalCollision) {
             double dir = MovementUtils.getDirection();
             double dist = distance.getValue();
             double x = -Math.sin(dir) * dist;
             double z = Math.cos(dir) * dist;

             if (mode.getMode().equals("Clip")) {
                 mc.player.setPosition(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z);
             } else if (mode.getMode().equals("Packet")) {
                 mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z, mc.player.isOnGround()));
                 mc.player.setPosition(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z);
             } else if (mode.getMode().equals("Sand")) {
                 // Sand phase uses falling block physics to clip through blocks
                 mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 0.000001, mc.player.getZ());
             }
        }
    }
}
