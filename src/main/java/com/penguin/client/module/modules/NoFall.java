package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class NoFall extends Module {

    private ModeSetting mode = new ModeSetting("Mode", "Packet", "Packet", "Bucket", "Web");

    public NoFall() {
        super("NoFall", "Prevents fall damage using packets, water bucket, or cobweb.", Category.MOVEMENT);
        addSetting(mode);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (mc.player.fallDistance > 3.0) {
            if (mode.getMode().equals("Packet")) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            } else if (mode.getMode().equals("Bucket") || mode.getMode().equals("Web")) {
                 Vec3d start = mc.player.getPos();
                 Vec3d end = start.add(0, -5, 0);
                 BlockHitResult result = mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));

                 if (result.getType() == HitResult.Type.BLOCK && start.y - result.getPos().y < 3) {
                     int slot = -1;
                     for (int i=0; i<9; i++) {
                         if (mode.getMode().equals("Bucket") && mc.player.getInventory().getStack(i).getItem() == Items.WATER_BUCKET) {
                             slot = i; break;
                         }
                         if (mode.getMode().equals("Web") && mc.player.getInventory().getStack(i).getItem() == Items.COBWEB) {
                             slot = i; break;
                         }
                     }

                     if (slot != -1) {
                         int prev = mc.player.getInventory().selectedSlot;
                         mc.player.getInventory().selectedSlot = slot;

                         float oldPitch = mc.player.getPitch();
                         mc.player.setPitch(90);

                         if (mode.getMode().equals("Bucket")) {
                             mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                         } else {
                             mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, result);
                         }

                         mc.player.setPitch(oldPitch);
                         mc.player.getInventory().selectedSlot = prev;
                     }
                 }
            }
        }
    }
}
