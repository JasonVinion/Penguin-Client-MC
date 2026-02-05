package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;

public class AntiEffect extends Module {
    public static AntiEffect INSTANCE;
    public BooleanSetting noBlindness = new BooleanSetting("No Blindness", true);
    public BooleanSetting noNausea = new BooleanSetting("No Nausea", true);
    public BooleanSetting noDarkness = new BooleanSetting("No Darkness", true);
    private double originalDistortion = 1.0;

    public AntiEffect() {
        super("AntiEffect", "Removes visual overlay from negative potion effects like nausea, blindness, and darkness.", Category.PLAYER);
        addSetting(noBlindness);
        addSetting(noNausea);
        addSetting(noDarkness);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (MinecraftClient.getInstance().options != null) {
            originalDistortion = MinecraftClient.getInstance().options.getDistortionEffectScale().getValue();
        }
    }

    @Override
    public void onDisable() {
        if (MinecraftClient.getInstance().options != null) {
            MinecraftClient.getInstance().options.getDistortionEffectScale().setValue(originalDistortion);
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        // Remove nausea effect by setting distortion to 0
        if (noNausea.isEnabled()) {
            if (mc.options != null) {
                mc.options.getDistortionEffectScale().setValue(0.0);
            }
        }
        
        // Remove effects from the player directly for immediate feedback
        if (noBlindness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
        }
        if (noDarkness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.DARKNESS)) {
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        }
        if (noNausea.isEnabled() && mc.player.hasStatusEffect(StatusEffects.NAUSEA)) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
        }
    }
}
