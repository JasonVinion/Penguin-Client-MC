package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.InventoryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {

    private BooleanSetting noBreak = new BooleanSetting("No Break", true);
    private NumberSetting durabilityThreshold = new NumberSetting("Durability %", 5, 1, 50, 1);
    private NumberSetting delay = new NumberSetting("Delay", 2, 0, 10, 1);

    private int timer = 0;

    public AutoArmor() {
        super("AutoArmor", "Automatically equips the best armor from your inventory.", Category.PLAYER);
        addSetting(noBreak);
        addSetting(durabilityThreshold);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (timer > 0) {
            timer--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        // Check for breaking armor first
        if (noBreak.isEnabled()) {
            for (int i = 0; i < 4; i++) {
                ItemStack stack = mc.player.getInventory().getArmorStack(i);
                if (!stack.isEmpty() && stack.isDamageable()) {
                    float damage = stack.getDamage();
                    float maxDamage = stack.getMaxDamage();
                    float durabilityPercent = ((maxDamage - damage) / maxDamage) * 100f;

                    if (durabilityPercent <= durabilityThreshold.getValue()) {
                        // Unequip
                        // Armor slots in inventory: 0-3 (Feet, Legs, Chest, Head)
                        // Armor slots in Container: 5 (Head), 6 (Chest), 7 (Legs), 8 (Feet)
                        int armorSlot = 8 - i;
                        // Quick move it out
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, armorSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer = (int) delay.getValue();
                        return;
                    }
                }
            }
        }

        // Equip best armor
        // Indices in getArmorStack: 0: Feet, 1: Legs, 2: Chest, 3: Head
        // EquipmentSlots: FEET, LEGS, CHEST, HEAD

        int[] bestArmorSlots = new int[4]; // Stores inventory slot index of best armor
        int[] bestArmorValues = new int[4];

        // Initialize with current armor values
        for (int i = 0; i < 4; i++) {
            bestArmorSlots[i] = -1;
            ItemStack currentStack = mc.player.getInventory().getArmorStack(i);
            if (!currentStack.isEmpty() && currentStack.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) currentStack.getItem();
                bestArmorValues[i] = armor.getProtection();
                if (noBreak.isEnabled() && currentStack.isDamageable()) {
                     float damage = currentStack.getDamage();
                     float maxDamage = currentStack.getMaxDamage();
                     float durabilityPercent = ((maxDamage - damage) / maxDamage) * 100f;
                     if (durabilityPercent <= durabilityThreshold.getValue()) {
                         bestArmorValues[i] = -1; // Treat as if we have nothing/bad armor
                     }
                }
            } else {
                bestArmorValues[i] = -1;
            }
        }

        // Scan inventory
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;

            ArmorItem armor = (ArmorItem) stack.getItem();
            int armorTypeIndex = getArmorTypeIndex(armor.getSlotType());
            if (armorTypeIndex == -1) continue;

            int protection = armor.getProtection();

            // Check durability of candidate
             if (noBreak.isEnabled() && stack.isDamageable()) {
                 float damage = stack.getDamage();
                 float maxDamage = stack.getMaxDamage();
                 float durabilityPercent = ((maxDamage - damage) / maxDamage) * 100f;
                 if (durabilityPercent <= durabilityThreshold.getValue()) continue;
             }

            if (protection > bestArmorValues[armorTypeIndex]) {
                bestArmorValues[armorTypeIndex] = protection;
                bestArmorSlots[armorTypeIndex] = slot;
            }
        }

        // Apply
        for (int i = 0; i < 4; i++) {
            int invSlot = bestArmorSlots[i];
            if (invSlot != -1) {
                // We found better armor
                // i=0 is FEET. Container Slot 8.
                // i=3 is HEAD. Container Slot 5.
                int armorSlot = 8 - i;
                InventoryUtils.equipArmor(invSlot, armorSlot);
                timer = (int) delay.getValue();
                return; // One per tick
            }
        }
    }

    private int getArmorTypeIndex(EquipmentSlot slot) {
        switch (slot) {
            case FEET: return 0;
            case LEGS: return 1;
            case CHEST: return 2;
            case HEAD: return 3;
            default: return -1;
        }
    }
}
