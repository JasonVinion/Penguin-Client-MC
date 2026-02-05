package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.HitResult;

public class AutoWeapon extends Module {
    private BooleanSetting preventBreak = new BooleanSetting("Prevent Breaking", false);
    private NumberSetting threshold = new NumberSetting("Threshold", 10, 1, 100, 1);
    private ModeSetting preference = new ModeSetting("Preference", "Sword", "Sword", "Axe");
    private BooleanSetting inventory = new BooleanSetting("Inventory", true);

    public AutoWeapon() {
        super("AutoWeapon", "Switches to strongest weapon in hotbar when attacking.", Category.COMBAT);
        addSetting(preventBreak);
        addSetting(threshold);
        addSetting(preference);
        addSetting(inventory);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (mc.options.attackKey.isPressed() && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            int bestSlot = -1;
            double bestScore = -1.0;

            int maxSlot = inventory.isEnabled() ? 36 : 9;

            for (int i = 0; i < maxSlot; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty()) continue;

                if (preventBreak.isEnabled() && stack.isDamageable() && (stack.getMaxDamage() - stack.getDamage()) < threshold.getValue()) {
                    continue;
                }

                if (!(stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem)) continue;

                double damage = 0;

                if (stack.getItem() instanceof SwordItem) {
                    damage += ((SwordItem) stack.getItem()).getAttackDamage() + 1.0;
                } else if (stack.getItem() instanceof AxeItem) {
                     damage += ((AxeItem) stack.getItem()).getAttackDamage() + 1.0;
                }

                int sharpness = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, stack);
                damage += sharpness * 0.5;

                if (preference.getMode().equals("Sword") && stack.getItem() instanceof SwordItem) damage += 100;
                if (preference.getMode().equals("Axe") && stack.getItem() instanceof AxeItem) damage += 100;

                if (damage > bestScore) {
                    bestScore = damage;
                    bestSlot = i;
                }
            }

            if (bestSlot != -1) {
                if (bestSlot < 9) {
                     if (mc.player.getInventory().selectedSlot != bestSlot) {
                         mc.player.getInventory().selectedSlot = bestSlot;
                     }
                } else {
                     int current = mc.player.getInventory().selectedSlot;
                     mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, bestSlot, current, SlotActionType.SWAP, mc.player);
                }
            }
        }
    }
}
