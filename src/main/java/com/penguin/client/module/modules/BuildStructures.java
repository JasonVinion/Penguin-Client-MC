package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Testing module that creates structures for testing.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class BuildStructures extends Module {
    private final NumberSetting size = new NumberSetting("Size", 10, 3, 50, 1);
    
    private final ActionSetting buildBox = new ActionSetting("Build Box", this::buildBox);
    private final ActionSetting buildPlatform = new ActionSetting("Build Platform", this::buildPlatform);
    private final ActionSetting buildWalls = new ActionSetting("Build Walls", this::buildWalls);
    private final ActionSetting clearArea = new ActionSetting("Clear Area", this::clearArea);

    public BuildStructures() {
        super("BuildStructures", "Creates structures for testing. Requires OP/Creative.", Category.TESTING);
        addSetting(size);
        addSetting(buildBox);
        addSetting(buildPlatform);
        addSetting(buildWalls);
        addSetting(clearArea);
    }

    @Override
    public void onEnable() {
        buildPlatform();
        this.toggle();
    }

    private void buildBox() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int s = (int) size.getValue();
        BlockPos pos = mc.player.getBlockPos();
        
        // Build hollow box
        String command = String.format("fill %d %d %d %d %d %d minecraft:stone hollow",
                pos.getX() - s/2, pos.getY(), pos.getZ() - s/2,
                pos.getX() + s/2, pos.getY() + s, pos.getZ() + s/2);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void buildPlatform() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int s = (int) size.getValue();
        BlockPos pos = mc.player.getBlockPos();
        
        // Build platform under player
        String command = String.format("fill %d %d %d %d %d %d minecraft:stone",
                pos.getX() - s/2, pos.getY() - 1, pos.getZ() - s/2,
                pos.getX() + s/2, pos.getY() - 1, pos.getZ() + s/2);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void buildWalls() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int s = (int) size.getValue();
        BlockPos pos = mc.player.getBlockPos();
        int halfS = s / 2;
        
        // Build 4 walls
        // North wall
        String cmd1 = String.format("fill %d %d %d %d %d %d minecraft:stone",
                pos.getX() - halfS, pos.getY(), pos.getZ() - halfS,
                pos.getX() + halfS, pos.getY() + 3, pos.getZ() - halfS);
        // South wall
        String cmd2 = String.format("fill %d %d %d %d %d %d minecraft:stone",
                pos.getX() - halfS, pos.getY(), pos.getZ() + halfS,
                pos.getX() + halfS, pos.getY() + 3, pos.getZ() + halfS);
        // East wall
        String cmd3 = String.format("fill %d %d %d %d %d %d minecraft:stone",
                pos.getX() + halfS, pos.getY(), pos.getZ() - halfS,
                pos.getX() + halfS, pos.getY() + 3, pos.getZ() + halfS);
        // West wall
        String cmd4 = String.format("fill %d %d %d %d %d %d minecraft:stone",
                pos.getX() - halfS, pos.getY(), pos.getZ() - halfS,
                pos.getX() - halfS, pos.getY() + 3, pos.getZ() + halfS);
        
        mc.player.networkHandler.sendChatCommand(cmd1);
        mc.player.networkHandler.sendChatCommand(cmd2);
        mc.player.networkHandler.sendChatCommand(cmd3);
        mc.player.networkHandler.sendChatCommand(cmd4);
    }

    private void clearArea() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int s = (int) size.getValue();
        BlockPos pos = mc.player.getBlockPos();
        
        // Clear area around player
        String command = String.format("fill %d %d %d %d %d %d minecraft:air",
                pos.getX() - s/2, pos.getY(), pos.getZ() - s/2,
                pos.getX() + s/2, pos.getY() + s, pos.getZ() + s/2);
        mc.player.networkHandler.sendChatCommand(command);
    }
}
