package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.StringSetting;
import com.penguin.client.ui.screen.ReplenishConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * HotbarRefill - Automatically refills hotbar stacks from inventory
 * 
 * When items in your hotbar get low (below threshold), this module
 * automatically moves matching items from your main inventory to 
 * refill the hotbar stack.
 */
public class Replenish extends Module {
    private NumberSetting threshold = new NumberSetting("Threshold", 16.0, 1.0, 63.0, 1.0);
    private NumberSetting delay = new NumberSetting("Delay", 5.0, 0.0, 20.0, 1.0);

    private BooleanSetting useLayout = new BooleanSetting("Use Layout", false);
    private ActionSetting editLayout = new ActionSetting("Edit Layout...", () -> {
        MinecraftClient.getInstance().setScreen(new ReplenishConfigScreen(MinecraftClient.getInstance().currentScreen, this));
    });
    private StringSetting layoutConfig = new StringSetting("Layout Config", "");

    private int timer = 0;

    // Layout Cache
    private Map<Integer, Item> cachedLayout = new HashMap<>();
    private String lastLayoutString = null;

    public Replenish() {
        super("HotbarRefill", "Automatically refills low hotbar stacks from inventory. Set threshold to control when refill triggers.", Category.PLAYER);
        addSetting(threshold);
        addSetting(delay);
        addSetting(useLayout);
        addSetting(editLayout);
        addSetting(layoutConfig);
    }

    public StringSetting getLayoutConfig() {
        return layoutConfig;
    }

    @Override
    public void onTick() {
        if (timer > 0) {
            timer--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (mc.currentScreen != null) return; // Don't swap while in inventory

        // Layout enforcement
        if (useLayout.isEnabled()) {
            updateLayoutCache();
            if (enforceLayout(mc)) return;
        }

        // Iterate hotbar slots (0-8 in Inventory, but 36-44 in Container)
        for (int i = 0; i < 9; i++) {
            int hotbarContainerSlot = i + 36;
            if (hotbarContainerSlot >= mc.player.currentScreenHandler.slots.size()) continue;

            ItemStack stack = mc.player.currentScreenHandler.slots.get(hotbarContainerSlot).getStack();

            if (!stack.isEmpty() && stack.getCount() <= threshold.getValue() && stack.isStackable()) {
                int inventorySlot = findItem(stack);
                if (inventorySlot != -1) {
                    // Pickup from inventory
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, inventorySlot, 0, SlotActionType.PICKUP, mc.player);

                    // Click on hotbar slot (Deposit/Swap)
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, hotbarContainerSlot, 0, SlotActionType.PICKUP, mc.player);

                    // Put back remainder if any (Cursor stack not empty)
                    if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, inventorySlot, 0, SlotActionType.PICKUP, mc.player);
                    }

                    timer = (int) delay.getValue();
                    return;
                }
            }
        }
    }

    private void updateLayoutCache() {
        String current = layoutConfig.getValue();
        if (current.equals(lastLayoutString)) return;

        lastLayoutString = current;
        cachedLayout.clear();
        if (current == null || current.isEmpty()) return;

        String[] parts = current.split(",");
        for (String part : parts) {
            String[] pair = part.split(":");
            if (pair.length == 2) {
                try {
                    int slot = Integer.parseInt(pair[0]);
                    Identifier id = Identifier.tryParse(pair[1]);
                    if (id != null) {
                        Item item = Registries.ITEM.get(id);
                        if (item != null) {
                            cachedLayout.put(slot, item);
                        }
                    }
                } catch (Exception ignored) { }
            }
        }
    }

    private boolean enforceLayout(MinecraftClient mc) {
        for (int i = 0; i < 9; i++) {
            if (!cachedLayout.containsKey(i)) continue;

            Item targetItem = cachedLayout.get(i);
            int hotbarContainerSlot = i + 36;
            if (hotbarContainerSlot >= mc.player.currentScreenHandler.slots.size()) continue;

            ItemStack stack = mc.player.currentScreenHandler.slots.get(hotbarContainerSlot).getStack();

            if (stack.getItem() != targetItem) {
                // Wrong item or empty, search in inventory
                int invSlot = findItem(targetItem);
                if (invSlot != -1) {
                    // Swap from inventory to hotbar
                    // SlotActionType.SWAP: button is hotbar slot index (0-8), slotId is source slot.
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, invSlot, i, SlotActionType.SWAP, mc.player);
                    timer = (int) delay.getValue();
                    return true;
                }
            }
        }
        return false;
    }

    private int findItem(Item item) {
        MinecraftClient mc = MinecraftClient.getInstance();
         for (int i = 9; i < 36; i++) { // 9-35 are main inventory
             if (i >= mc.player.currentScreenHandler.slots.size()) continue;
             ItemStack s = mc.player.currentScreenHandler.slots.get(i).getStack();
             if (!s.isEmpty() && s.getItem() == item) {
                 return i;
             }
        }
        return -1;
    }

    private int findItem(ItemStack match) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (int i = 9; i < 36; i++) {
             if (i >= mc.player.currentScreenHandler.slots.size()) continue;
             ItemStack s = mc.player.currentScreenHandler.slots.get(i).getStack();
             if (!s.isEmpty() && s.getItem() == match.getItem() && ItemStack.canCombine(s, match)) {
                 return i;
             }
        }
        return -1;
    }
}
