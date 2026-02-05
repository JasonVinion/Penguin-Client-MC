package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.entity.Entity;

public class Criticals extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Packet", "Packet", "Jump");

    public Criticals() {
        super("Criticals", "Always deal critical hits. Packet mode fakes a jump server-side, Jump mode performs an actual client-side jump.", Category.COMBAT);
        addSetting(mode);
    }

    public static void onAttack(Entity target) {
        Criticals module = ModuleManager.INSTANCE.getModule(Criticals.class);
        if (module != null && module.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.player.isOnGround()) {
                if (module.mode.getMode().equals("Packet")) {
                    double x = mc.player.getX();
                    double y = mc.player.getY();
                    double z = mc.player.getZ();

                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
                    mc.player.addCritParticles(target);
                } else if (module.mode.getMode().equals("Jump")) {
                    // Perform actual client-side jump for visual feedback
                    mc.player.jump();
                    
                    // Also send packets for server confirmation
                    double x = mc.player.getX();
                    double y = mc.player.getY();
                    double z = mc.player.getZ();
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.42, z, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.75, z, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1.0, z, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.75, z, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.42, z, false));
                    mc.player.addCritParticles(target);
                }
            }
        }
    }
}
