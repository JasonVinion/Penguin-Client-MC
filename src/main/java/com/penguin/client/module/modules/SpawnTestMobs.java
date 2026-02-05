package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.ActionSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Testing module that spawns mobs for testing combat modules.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class SpawnTestMobs extends Module {
    private final ModeSetting mobType = new ModeSetting("Mob Type", "Zombie", 
            "Zombie", "Skeleton", "Creeper", "Spider", "Enderman", "Pig", "Cow", "Sheep", "Villager");
    private final NumberSetting count = new NumberSetting("Count", 5, 1, 50, 1);
    private final NumberSetting radius = new NumberSetting("Radius", 5, 1, 20, 1);

    private final ActionSetting spawnMobs = new ActionSetting("Spawn Mobs", this::spawnMobs);
    private final ActionSetting spawnTrapped = new ActionSetting("Spawn Trapped", this::spawnTrappedMobs);
    private final ActionSetting clearMobs = new ActionSetting("Clear Nearby Mobs", this::clearMobs);

    public SpawnTestMobs() {
        super("SpawnTestMobs", "Toggle to spawn mobs, or use action buttons for more options. Requires OP/Creative.", Category.TESTING);
        addSetting(mobType);
        addSetting(count);
        addSetting(radius);
        addSetting(spawnMobs);
        addSetting(spawnTrapped);
        addSetting(clearMobs);
    }
    
    @Override
    public void onEnable() {
        // When module is toggled on, spawn mobs immediately and then disable
        spawnMobs();
        // Auto-disable after spawning
        this.toggle();
    }

    private void spawnMobs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String mob = mobType.getMode().toLowerCase();
        int c = (int) count.getValue();
        int r = (int) radius.getValue();

        BlockPos playerPos = mc.player.getBlockPos();
        
        for (int i = 0; i < c; i++) {
            double angle = (2 * Math.PI * i) / c;
            int x = playerPos.getX() + (int) (r * Math.cos(angle));
            int z = playerPos.getZ() + (int) (r * Math.sin(angle));
            
            String command = String.format("summon minecraft:%s %d %d %d", 
                    mob, x, playerPos.getY(), z);
            mc.player.networkHandler.sendChatCommand(command);
        }
    }

    private void spawnTrappedMobs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String mob = mobType.getMode().toLowerCase();
        int c = (int) count.getValue();
        int r = (int) radius.getValue();

        BlockPos playerPos = mc.player.getBlockPos();
        
        for (int i = 0; i < c; i++) {
            double angle = (2 * Math.PI * i) / c;
            int x = playerPos.getX() + (int) (r * Math.cos(angle));
            int z = playerPos.getZ() + (int) (r * Math.sin(angle));
            int y = playerPos.getY();
            
            // Create a fence trap around the mob
            String fenceCommand = String.format("fill %d %d %d %d %d %d minecraft:oak_fence",
                    x - 1, y, z - 1, x + 1, y + 2, z + 1);
            mc.player.networkHandler.sendChatCommand(fenceCommand);
            
            // Clear the inside
            String airCommand = String.format("fill %d %d %d %d %d %d minecraft:air",
                    x, y, z, x, y + 1, z);
            mc.player.networkHandler.sendChatCommand(airCommand);
            
            // Spawn the mob
            String mobCommand = String.format("summon minecraft:%s %d %d %d", 
                    mob, x, y, z);
            mc.player.networkHandler.sendChatCommand(mobCommand);
        }
    }

    private void clearMobs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int r = (int) radius.getValue() * 2;
        
        // Kill all nearby mobs
        String command = String.format("kill @e[type=!player,distance=..%d]", r);
        mc.player.networkHandler.sendChatCommand(command);
    }
}
