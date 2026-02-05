package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Testing module that spawns powder snow for testing movement modules.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class SpawnPowderSnow extends Module {

    private final ActionSetting spawnCube = new ActionSetting("Spawn 3x3x3 Cube", this::spawnPowderSnowCube);
    private final ActionSetting spawnFloor = new ActionSetting("Spawn 10x10 Floor", this::spawnPowderSnowFloor);
    private final ActionSetting clearArea = new ActionSetting("Clear Area", this::clearArea);

    public SpawnPowderSnow() {
        super("SpawnPowderSnow", "Spawns powder snow blocks for testing PowderSnowWalk. Requires OP/Creative.", Category.TESTING);
        addSetting(spawnCube);
        addSetting(spawnFloor);
        addSetting(clearArea);
    }

    private void spawnPowderSnowCube() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int x = playerPos.getX() + 3;
        int y = playerPos.getY();
        int z = playerPos.getZ();

        String command = String.format("fill %d %d %d %d %d %d minecraft:powder_snow",
                x, y, z, x + 2, y + 2, z + 2);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void spawnPowderSnowFloor() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int x = playerPos.getX();
        int y = playerPos.getY() - 1;
        int z = playerPos.getZ();

        String command = String.format("fill %d %d %d %d %d %d minecraft:powder_snow",
                x - 5, y, z - 5, x + 5, y, z + 5);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void clearArea() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int x = playerPos.getX();
        int y = playerPos.getY();
        int z = playerPos.getZ();

        // Clear a 10x10x5 area around the player
        String command = String.format("fill %d %d %d %d %d %d minecraft:air replace minecraft:powder_snow",
                x - 5, y - 2, z - 5, x + 5, y + 3, z + 5);
        mc.player.networkHandler.sendChatCommand(command);
    }
}
