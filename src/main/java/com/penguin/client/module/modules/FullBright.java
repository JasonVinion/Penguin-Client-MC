package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullBright extends Module {

    private double originalGamma = 1.0;
    private ModeSetting mode = new ModeSetting("Mode", "Gamma", "Gamma", "Potion");
    private NumberSetting value = new NumberSetting("Gamma", 100.0, 1.0, 1000.0, 1.0);

    public FullBright() {
        super("Fullbright", "Makes everything fully lit so you can see in the dark.", Category.RENDER);
        addSetting(mode);
        addSetting(value);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            originalGamma = mc.options.getGamma().getValue();
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            mc.options.getGamma().setValue(originalGamma);
        }
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mode.getMode().equals("Gamma")) {
            if (mc.options != null && mc.options.getGamma().getValue() != value.getValue()) {
                mc.options.getGamma().setValue(value.getValue());
            }
            if (mc.player != null) {
                 mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        } else {
            // Potion Mode
            if (mc.options != null) {
                 mc.options.getGamma().setValue(originalGamma);
            }
            if (mc.player != null) {
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 400, 0, false, false));
            }
        }
    }
}
