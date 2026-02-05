package com.penguin.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryUtils {

    public static int getSlot(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                if (i < 9) return i + 36; // Hotbar in container mapping
                return i;
            }
        }
        return -1;
    }

    public static int getSlotHotbar(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static void moveItem(int fromSlot, int toSlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        ItemStack fromStack = mc.player.currentScreenHandler.getSlot(fromSlot).getStack();

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, toSlot, 0, SlotActionType.PICKUP, mc.player);
        
        // Only perform third click if there was an item to move
        if (!fromStack.isEmpty()) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fromSlot, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    // Quick equip helper for auto armor
    // Armor slots: 5 (Head), 6 (Chest), 7 (Legs), 8 (Feet)
    public static void equipArmor(int itemSlot, int armorSlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        // Container Slot Mapping for Player Inventory:
        // 0: Crafting Output
        // 1-4: Crafting Input
        // 5-8: Armor
        // 9-35: Storage
        // 36-44: Hotbar

        // Item slot coming from `getInventory().getStack(i)`:
        // 0-8: Hotbar -> 36-44
        // 9-35: Storage -> 9-35

        int correctItemSlot = itemSlot;
        if (itemSlot < 9) correctItemSlot = 36 + itemSlot;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, correctItemSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.PICKUP, mc.player);
        // If we held something (old armor), we need to put it back?
        // Actually if we click an armor slot with a valid item, it swaps. The old armor is now on cursor.
        // We put it in the old item slot.
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, correctItemSlot, 0, SlotActionType.PICKUP, mc.player);
    }
}
