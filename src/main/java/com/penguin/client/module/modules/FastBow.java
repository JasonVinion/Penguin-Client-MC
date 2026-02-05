package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FastBow extends Module {
    private NumberSetting ticks = new NumberSetting("Ticks", 20, 1, 50, 1);
    private ModeSetting mode = new ModeSetting("Mode", "Hold", "Hold", "Toggle");

    public FastBow() {
        super("FastBow", "Rapidly shoots arrows. Hold mode requires holding right click, Toggle mode auto-charges after each shot.", Category.COMBAT);
        addSetting(ticks);
        addSetting(mode);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        boolean holdingBow = mc.player.getMainHandStack().getItem() == Items.BOW || 
                             mc.player.getOffHandStack().getItem() == Items.BOW;
        if (!holdingBow) return;

        if (mc.player.isUsingItem()) {
             if (mc.player.getItemUseTime() >= 3) {
                 // Determine which hand has the bow
                 Hand bowHand = mc.player.getMainHandStack().getItem() == Items.BOW ? Hand.MAIN_HAND : Hand.OFF_HAND;
                 
                 // Stop using item first to maintain client-server sync
                 mc.player.stopUsingItem();
                 mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));

                 for (int i = 0; i < ticks.getValue(); i++) {
                     mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
                 }
                 
                 // Both modes should restart the bow if player is still holding use key
                 // Toggle mode always restarts, Hold mode only if use key is pressed
                 boolean shouldRestart = mode.getMode().equals("Toggle") || mc.options.useKey.isPressed();
                 
                 if (shouldRestart) {
                     mc.interactionManager.interactItem(mc.player, bowHand);
                 }
             }
        } else {
            // For Hold mode: if player is holding use key but not using item, start using bow
            if (mode.getMode().equals("Hold") && mc.options.useKey.isPressed()) {
                Hand bowHand = mc.player.getMainHandStack().getItem() == Items.BOW ? Hand.MAIN_HAND : Hand.OFF_HAND;
                mc.interactionManager.interactItem(mc.player, bowHand);
            }
        }
    }
}
