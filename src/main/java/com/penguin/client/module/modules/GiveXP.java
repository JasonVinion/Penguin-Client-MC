package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that gives the player experience levels.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class GiveXP extends Module {
    private final NumberSetting levels = new NumberSetting("Levels", 30, 1, 100, 1);
    
    private final ActionSetting addLevels = new ActionSetting("Add Levels", this::addLevels);
    private final ActionSetting setLevels = new ActionSetting("Set Levels", this::setLevels);
    private final ActionSetting maxLevels = new ActionSetting("Set Max (100)", this::maxLevels);

    public GiveXP() {
        super("GiveXP", "Gives player experience levels. Requires OP.", Category.TESTING);
        addSetting(levels);
        addSetting(addLevels);
        addSetting(setLevels);
        addSetting(maxLevels);
    }

    @Override
    public void onEnable() {
        addLevels();
        this.toggle();
    }

    private void addLevels() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int lvl = (int) levels.getValue();
        String command = String.format("xp add @s %d levels", lvl);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void setLevels() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int lvl = (int) levels.getValue();
        String command = String.format("xp set @s %d levels", lvl);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void maxLevels() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("xp set @s 100 levels");
    }
}
