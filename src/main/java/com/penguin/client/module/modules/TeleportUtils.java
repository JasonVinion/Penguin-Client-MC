package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that teleports the player to various locations.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class TeleportUtils extends Module {
    private final NumberSetting x = new NumberSetting("X", 0, -30000000, 30000000, 1);
    private final NumberSetting y = new NumberSetting("Y", 100, -64, 320, 1);
    private final NumberSetting z = new NumberSetting("Z", 0, -30000000, 30000000, 1);
    
    private final ActionSetting teleportToCoords = new ActionSetting("Teleport to Coords", this::teleportToCoords);
    private final ActionSetting teleportToSpawn = new ActionSetting("Teleport to Spawn", this::teleportToSpawn);
    private final ActionSetting teleportUp100 = new ActionSetting("Teleport Up 100", this::teleportUp100);
    private final ActionSetting teleportToBedrock = new ActionSetting("Teleport to Y=-60", this::teleportToBedrock);

    public TeleportUtils() {
        super("TeleportUtils", "Teleports player to various locations. Requires OP.", Category.TESTING);
        addSetting(x);
        addSetting(y);
        addSetting(z);
        addSetting(teleportToCoords);
        addSetting(teleportToSpawn);
        addSetting(teleportUp100);
        addSetting(teleportToBedrock);
    }

    @Override
    public void onEnable() {
        teleportToCoords();
        this.toggle();
    }

    private void teleportToCoords() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int tpX = (int) x.getValue();
        int tpY = (int) y.getValue();
        int tpZ = (int) z.getValue();
        
        String command = String.format("tp @s %d %d %d", tpX, tpY, tpZ);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void teleportToSpawn() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("tp @s 0 100 0");
    }

    private void teleportUp100() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("tp @s ~ ~100 ~");
    }

    private void teleportToBedrock() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("tp @s ~ -60 ~");
    }
}
