package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Hand;

public class AutoPot extends Module {
    private NumberSetting health = new NumberSetting("Health", 10.0, 1.0, 20.0, 1.0);
    private BooleanSetting healthPot = new BooleanSetting("Health Pot", true);
    private BooleanSetting speedPot = new BooleanSetting("Speed Pot", true);
    private BooleanSetting strengthPot = new BooleanSetting("Strength Pot", true);
    private int delay = 0;

    public AutoPot() {
        super("AutoPot", "Automatically throws healing and buff potions when needed.", Category.COMBAT);
        addSetting(health);
        addSetting(healthPot);
        addSetting(speedPot);
        addSetting(strengthPot);
    }

    @Override
    public void onTick() {
        if (delay > 0) {
            delay--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (healthPot.isEnabled() && mc.player.getHealth() <= health.getValue()) {
            if (throwPotion(Potions.STRONG_HEALING, Potions.HEALING)) {
                delay = 10;
                return;
            }
        }

        if (speedPot.isEnabled() && !mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            if (throwPotion(Potions.STRONG_SWIFTNESS, Potions.SWIFTNESS, Potions.LONG_SWIFTNESS)) {
                delay = 10;
                return;
            }
        }

        if (strengthPot.isEnabled() && !mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
            if (throwPotion(Potions.STRONG_STRENGTH, Potions.STRENGTH, Potions.LONG_STRENGTH)) {
                delay = 10;
                return;
            }
        }
    }

    private boolean throwPotion(Potion... potions) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int slot = findPotion(potions);
        if (slot != -1) {
            int oldSlot = mc.player.getInventory().selectedSlot;
            float oldPitch = mc.player.getPitch();

            mc.player.setPitch(90);
            mc.player.getInventory().selectedSlot = slot;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = oldSlot;
            mc.player.setPitch(oldPitch);
            return true;
        }
        return false;
    }

    private int findPotion(Potion... targets) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SplashPotionItem) {
                Potion p = PotionUtil.getPotion(stack);
                for (Potion target : targets) {
                    if (p == target) return i;
                }
            }
        }
        return -1;
    }
}
