package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoTool extends Module {
    private BooleanSetting preventBreak = new BooleanSetting("Prevent Breaking", false);
    private NumberSetting threshold = new NumberSetting("Threshold", 10, 1, 100, 1);
    private ModeSetting preference = new ModeSetting("Preference", "Efficiency", "Efficiency", "Fortune");
    private BooleanSetting inventory = new BooleanSetting("Inventory", true);

    public AutoTool() {
        super("AutoTool", "Automatically switches to the best tool for the block you are mining.", Category.PLAYER);
        addSetting(preventBreak);
        addSetting(threshold);
        addSetting(preference);
        addSetting(inventory);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (mc.options.attackKey.isPressed() && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
            BlockState state = mc.world.getBlockState(hit.getBlockPos());
            int bestSlot = -1;
            float bestScore = -1.0f;

            // Iterate Hotbar (0-8) and Inventory (9-35)
            int maxSlot = inventory.isEnabled() ? 36 : 9;

            for (int i = 0; i < maxSlot; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty()) continue;

                if (preventBreak.isEnabled()) {
                    if (stack.isDamageable() && (stack.getMaxDamage() - stack.getDamage()) < threshold.getValue()) {
                        continue;
                    }
                }

                float speed = stack.getMiningSpeedMultiplier(state);
                if (speed <= 1.0f) continue; // Not effective tool

                // Calculate score based on speed and preference
                float score = speed;

                if (preference.getMode().equals("Efficiency")) {
                    score += EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack) * 10;
                } else if (preference.getMode().equals("Fortune")) {
                    score += EnchantmentHelper.getLevel(Enchantments.FORTUNE, stack) * 10;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }

            if (bestSlot != -1) {
                if (bestSlot < 9) {
                     if (mc.player.getInventory().selectedSlot != bestSlot) {
                         mc.player.getInventory().selectedSlot = bestSlot;
                     }
                } else {
                     // Swap from inventory to hotbar
                     int current = mc.player.getInventory().selectedSlot;
                     mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, bestSlot, current, SlotActionType.SWAP, mc.player);
                     // Note: This swaps the item into the hotbar immediately.
                     // The player's selected slot now holds the best tool.
                }
            }
        }
    }
}
