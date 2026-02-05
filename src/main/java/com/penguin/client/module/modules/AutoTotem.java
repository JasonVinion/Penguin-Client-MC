package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() {
        super("AutoTotem", "Moves totem of undying to offhand automatically.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Check Offhand
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        // Find Totem
        int slot = -1;
        for (int i = 0; i < 45; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                slot = i;
                break;
            }
        }

        if (slot != -1) {
            // Move to offhand
            // Slots in Container:
            // 0-4: Crafting
            // 5-8: Armor
            // 9-35: Inventory
            // 36-44: Hotbar
            // 45: Offhand

            // PlayerInventory (0-8 hotbar, 9-35 storage, 36-39 armor, 40 offhand) mapping is different from ScreenHandler slots.
            // Default ScreenHandler (PlayerScreenHandler):
            // 0: Craft output
            // 1-4: Craft input
            // 5-8: Armor
            // 9-35: Storage (PlayerInv 9-35)
            // 36-44: Hotbar (PlayerInv 0-8)
            // 45: Offhand

            // So if we iterate PlayerInventory 0-8 (Hotbar), that maps to 36-44 in ScreenHandler.
            // PlayerInv 9-35 maps to 9-35.

            int screenSlot = -1;
            if (slot >= 0 && slot < 9) { // Hotbar
                screenSlot = 36 + slot;
            } else if (slot >= 9 && slot < 36) { // Storage
                screenSlot = slot;
            } else if (slot == 40) { // Offhand in PlayerInv
                screenSlot = 45;
            }

            if (screenSlot != -1) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                // Put back if holding something?
                // If cursor has item, click original slot
                if (!mc.player.playerScreenHandler.getCursorStack().isEmpty()) {
                     mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
                }
            }
        }
    }
}
