package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that heals and feeds the player.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class HealAndFeed extends Module {
    private final NumberSetting healthAmount = new NumberSetting("Health", 20, 1, 20, 1);
    private final NumberSetting foodAmount = new NumberSetting("Food Level", 20, 0, 20, 1);
    
    private final ActionSetting heal = new ActionSetting("Heal", this::heal);
    private final ActionSetting feed = new ActionSetting("Feed", this::feed);
    private final ActionSetting healAndFeed = new ActionSetting("Heal & Feed", this::healAndFeed);

    public HealAndFeed() {
        super("HealAndFeed", "Heals and feeds the player instantly. Requires OP/Creative.", Category.TESTING);
        addSetting(healthAmount);
        addSetting(foodAmount);
        addSetting(heal);
        addSetting(feed);
        addSetting(healAndFeed);
    }

    @Override
    public void onEnable() {
        healAndFeed();
        this.toggle();
    }

    private void heal() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int health = (int) healthAmount.getValue();
        // Instant health amplifier: 0 = 4 health, 1 = 8 health, 2 = 12 health, etc.
        int amplifier = Math.max(0, (health / 4) - 1);
        String command = String.format("effect give @s minecraft:instant_health 1 %d true", amplifier);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void feed() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int food = (int) foodAmount.getValue();
        String command = String.format("effect give @s minecraft:saturation 1 %d true", food);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void healAndFeed() {
        heal();
        feed();
    }
}
