package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that gives the player powerful potion effects.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class GiveEffects extends Module {
    private final ModeSetting effectPreset = new ModeSetting("Effect Preset", "Speed",
            "Speed", "Strength", "Resistance", "Regeneration", "Invisibility", 
            "Night Vision", "Water Breathing", "Fire Resistance", "All Buffs");
    
    private final NumberSetting duration = new NumberSetting("Duration (sec)", 300, 10, 3600, 10);
    private final NumberSetting amplifier = new NumberSetting("Amplifier", 1, 0, 5, 1);
    
    private final ActionSetting applyEffect = new ActionSetting("Apply Effect", this::applyEffect);
    private final ActionSetting applyAllBuffs = new ActionSetting("Apply All Buffs", this::applyAllBuffs);

    public GiveEffects() {
        super("GiveEffects", "Gives player powerful potion effects. Requires OP/Creative.", Category.TESTING);
        addSetting(effectPreset);
        addSetting(duration);
        addSetting(amplifier);
        addSetting(applyEffect);
        addSetting(applyAllBuffs);
    }

    @Override
    public void onEnable() {
        applyEffect();
        this.toggle();
    }

    private void applyEffect() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String preset = effectPreset.getMode();
        int dur = (int) duration.getValue();
        int amp = (int) amplifier.getValue();

        if (preset.equals("All Buffs")) {
            applyAllBuffs();
            return;
        }

        String effect = getEffectName(preset);
        String command = String.format("effect give @s minecraft:%s %d %d true", effect, dur, amp);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void applyAllBuffs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int dur = (int) duration.getValue();
        int amp = (int) amplifier.getValue();

        String[] buffs = {
            "speed", "strength", "resistance", "regeneration", 
            "fire_resistance", "water_breathing", "night_vision"
        };

        for (String buff : buffs) {
            String command = String.format("effect give @s minecraft:%s %d %d true", buff, dur, amp);
            mc.player.networkHandler.sendChatCommand(command);
        }
    }

    private String getEffectName(String preset) {
        switch (preset) {
            case "Speed": return "speed";
            case "Strength": return "strength";
            case "Resistance": return "resistance";
            case "Regeneration": return "regeneration";
            case "Invisibility": return "invisibility";
            case "Night Vision": return "night_vision";
            case "Water Breathing": return "water_breathing";
            case "Fire Resistance": return "fire_resistance";
            default: return "speed";
        }
    }
}
