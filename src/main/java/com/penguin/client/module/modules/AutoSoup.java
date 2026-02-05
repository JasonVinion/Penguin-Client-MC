package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.item.Item;

public class AutoSoup extends Module {
    public AutoSoup() {
        super("AutoSoup", "Automatically consumes mushroom stew when health is low.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (mc.player.getHealth() < 14) {
             int soupSlot = -1;
             for (int i = 0; i < 9; i++) {
                 Item item = mc.player.getInventory().getStack(i).getItem();
                 if (item == Items.MUSHROOM_STEW || item == Items.BEETROOT_SOUP) {
                     soupSlot = i;
                     break;
                 }
             }

             if (soupSlot != -1) {
                 mc.player.getInventory().selectedSlot = soupSlot;
                 mc.options.useKey.setPressed(true);
             } else {
                 mc.options.useKey.setPressed(false);
             }
        } else {
             mc.options.useKey.setPressed(false);
        }
    }
}
