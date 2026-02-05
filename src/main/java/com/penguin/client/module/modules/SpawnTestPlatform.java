package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.ActionSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * Testing module that spawns a grass platform in the sky for testing.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class SpawnTestPlatform extends Module {
    private final NumberSetting width = new NumberSetting("Width", 100, 10, 200, 10);
    private final NumberSetting length = new NumberSetting("Length", 100, 10, 200, 10);
    private final NumberSetting height = new NumberSetting("Height", 3, 1, 10, 1);
    private final NumberSetting elevation = new NumberSetting("Elevation", 50, 10, 100, 10);

    private final ActionSetting spawnPlatform = new ActionSetting("Spawn Platform", this::createPlatform);

    public SpawnTestPlatform() {
        super("TestPlatform", "Spawns a grass block platform in the sky above the player. Requires OP/Creative.", Category.TESTING);
        addSetting(width);
        addSetting(length);
        addSetting(height);
        addSetting(elevation);
        addSetting(spawnPlatform);
    }

    private void createPlatform() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        int w = (int) width.getValue();
        int l = (int) length.getValue();
        int h = (int) height.getValue();
        int elev = (int) elevation.getValue();

        BlockPos playerPos = mc.player.getBlockPos();
        int startX = playerPos.getX() - w / 2;
        int startY = playerPos.getY() + elev;
        int startZ = playerPos.getZ() - l / 2;

        // Use fill command for efficiency (requires OP)
        String command = String.format("fill %d %d %d %d %d %d minecraft:grass_block",
                startX, startY, startZ,
                startX + w - 1, startY + h - 1, startZ + l - 1);
        
        mc.player.networkHandler.sendChatCommand(command);
        
        // Teleport player to the platform
        String tpCommand = String.format("tp @s %d %d %d", 
                playerPos.getX(), startY + h + 1, playerPos.getZ());
        mc.player.networkHandler.sendChatCommand(tpCommand);
    }

    @Override
    public void onEnable() {
        createPlatform();
        toggle(); // Auto-disable after use
    }
}
