package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class AutoEat extends Module {
    private int oldSlot = -1;
    private boolean eating = false;

    public AutoEat() {
        super("AutoEat", "Automatically eats food when your hunger drops below threshold.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (eating) {
            if (mc.player.getHungerManager().getFoodLevel() >= 18 || !mc.player.isUsingItem()) {
                eating = false;
                mc.options.useKey.setPressed(false);
                if (oldSlot != -1) {
                    mc.player.getInventory().selectedSlot = oldSlot;
                    oldSlot = -1;
                }
            } else {
                mc.options.useKey.setPressed(true);
            }
            return;
        }

        if (mc.player.getHungerManager().getFoodLevel() < 18) {
             int foodSlot = -1;
             for (int i = 0; i < 9; i++) {
                 ItemStack stack = mc.player.getInventory().getStack(i);
                 if (stack.isFood()) {
                     foodSlot = i;
                     break;
                 }
             }

             if (foodSlot != -1) {
                 oldSlot = mc.player.getInventory().selectedSlot;
                 mc.player.getInventory().selectedSlot = foodSlot;
                 eating = true;
                 mc.options.useKey.setPressed(true);
                 mc.interactionManager.interactItem(mc.player, mc.player.getActiveHand());
             }
        }
    }
}
