package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that changes the player's game mode.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class GameModeSwitch extends Module {
    private final ModeSetting gameMode = new ModeSetting("Mode", "Creative",
            "Survival", "Creative", "Adventure", "Spectator");
    
    private final ActionSetting applyMode = new ActionSetting("Apply Mode", this::applyMode);

    public GameModeSwitch() {
        super("GameModeSwitch", "Switches game mode instantly. Requires OP.", Category.TESTING);
        addSetting(gameMode);
        addSetting(applyMode);
    }

    @Override
    public void onEnable() {
        applyMode();
        this.toggle();
    }

    private void applyMode() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String mode = gameMode.getMode().toLowerCase();
        String command = "gamemode " + mode;
        mc.player.networkHandler.sendChatCommand(command);
    }
}
