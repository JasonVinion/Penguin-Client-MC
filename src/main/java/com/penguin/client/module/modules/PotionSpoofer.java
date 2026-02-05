package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class PotionSpoofer extends Module {
    public BooleanSetting speed = new BooleanSetting("Speed", false);
    public BooleanSetting jump = new BooleanSetting("Jump Boost", false);
    public BooleanSetting strength = new BooleanSetting("Strength", false);
    public BooleanSetting nightVision = new BooleanSetting("Night Vision", false);

    public PotionSpoofer() {
        super("PotionSpoofer", "Displays fake potion effects on your screen for visual purposes.", Category.RENDER);
        addSetting(speed);
        addSetting(jump);
        addSetting(strength);
        addSetting(nightVision);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (speed.isEnabled()) mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 1, false, false));
        if (jump.isEnabled()) mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 400, 1, false, false));
        if (strength.isEnabled()) mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 400, 1, false, false));
        if (nightVision.isEnabled()) mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false));
    }
}
