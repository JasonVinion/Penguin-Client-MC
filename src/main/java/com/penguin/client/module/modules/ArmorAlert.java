package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * ArmorAlert - Alerts you and optionally other players when armor durability is low
 */
public class ArmorAlert extends Module {
    private NumberSetting threshold = new NumberSetting("Threshold %", 20.0, 1.0, 50.0, 1.0);
    private BooleanSetting chat = new BooleanSetting("Chat Alert", true);
    private BooleanSetting sound = new BooleanSetting("Sound Alert", true);
    private BooleanSetting broadcast = new BooleanSetting("Broadcast", false);
    
    private boolean alerted = false;

    public ArmorAlert() {
        super("ArmorAlert", "Alerts when your armor durability is low.", Category.PLAYER);
        addSetting(threshold);
        addSetting(chat);
        addSetting(sound);
        addSetting(broadcast);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean shouldAlert = false;
        String lowPiece = "";
        
        for (ItemStack armor : mc.player.getArmorItems()) {
            if (armor.isEmpty()) continue;
            if (!armor.isDamageable()) continue;
            
            int maxDurability = armor.getMaxDamage();
            int currentDurability = maxDurability - armor.getDamage();
            double percentage = (currentDurability / (double) maxDurability) * 100;
            
            if (percentage <= threshold.getValue() && percentage > 0) {
                shouldAlert = true;
                lowPiece = armor.getName().getString();
                break;
            }
        }
        
        if (shouldAlert && !alerted) {
            if (chat.isEnabled()) {
                mc.player.sendMessage(Text.literal("§c[ArmorAlert] Low durability on " + lowPiece + "!"), false);
            }
            
            if (sound.isEnabled()) {
                mc.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
            }
            
            if (broadcast.isEnabled()) {
                mc.player.networkHandler.sendChatMessage("My " + lowPiece + " is breaking!");
            }
            
            alerted = true;
        } else if (!shouldAlert) {
            alerted = false;
        }
    }
}
