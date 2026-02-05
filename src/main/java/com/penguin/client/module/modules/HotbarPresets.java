package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that sets up predefined hotbar configurations.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class HotbarPresets extends Module {
    private final ModeSetting preset = new ModeSetting("Preset", "Combat",
            "Combat", "Mining", "Building", "Exploration", "PvP", "Farming");

    private final ActionSetting applyPreset = new ActionSetting("Apply Preset", this::applyPreset);
    private final ActionSetting clearHotbar = new ActionSetting("Clear Hotbar", this::clearHotbar);

    public HotbarPresets() {
        super("HotbarPresets", "Sets up predefined hotbar configurations for testing. Requires OP/Creative.", Category.TESTING);
        addSetting(preset);
        addSetting(applyPreset);
        addSetting(clearHotbar);
    }

    @Override
    public void onEnable() {
        // Apply preset when enabled, then disable
        applyPreset();
        this.toggle();
    }

    private void applyPreset() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Clear hotbar first
        clearHotbar();

        String selectedPreset = preset.getMode();
        switch (selectedPreset) {
            case "Combat":
                giveItem("diamond_sword", 0);
                giveItem("shield", 1);
                giveItem("bow", 2);
                giveItem("arrow", 3, 64);
                giveItem("golden_apple", 4, 16);
                giveItem("ender_pearl", 5, 16);
                giveItem("totem_of_undying", 6);
                giveItem("cooked_beef", 7, 64);
                giveItem("water_bucket", 8);
                break;
            case "Mining":
                giveItem("diamond_pickaxe", 0);
                giveItem("diamond_shovel", 1);
                giveItem("torch", 2, 64);
                giveItem("water_bucket", 3);
                giveItem("cobblestone", 4, 64);
                giveItem("ladder", 5, 64);
                giveItem("cooked_beef", 6, 64);
                giveItem("crafting_table", 7);
                giveItem("furnace", 8);
                break;
            case "Building":
                giveItem("stone", 0, 64);
                giveItem("oak_planks", 1, 64);
                giveItem("glass", 2, 64);
                giveItem("oak_stairs", 3, 64);
                giveItem("oak_slab", 4, 64);
                giveItem("oak_door", 5, 16);
                giveItem("torch", 6, 64);
                giveItem("scaffolding", 7, 64);
                giveItem("water_bucket", 8);
                break;
            case "Exploration":
                giveItem("diamond_sword", 0);
                giveItem("diamond_pickaxe", 1);
                giveItem("torch", 2, 64);
                giveItem("cooked_beef", 3, 64);
                giveItem("elytra", 4);
                giveItem("firework_rocket", 5, 64);
                giveItem("ender_pearl", 6, 16);
                giveItem("compass", 7);
                giveItem("map", 8);
                break;
            case "PvP":
                giveItem("netherite_sword", 0);
                giveItem("end_crystal", 1, 64);
                giveItem("obsidian", 2, 64);
                giveItem("ender_pearl", 3, 16);
                giveItem("golden_apple", 4, 64);
                giveItem("enchanted_golden_apple", 5, 8);
                giveItem("totem_of_undying", 6);
                giveItem("bow", 7);
                giveItem("arrow", 8, 64);
                break;
            case "Farming":
                giveItem("diamond_hoe", 0);
                giveItem("water_bucket", 1);
                giveItem("wheat_seeds", 2, 64);
                giveItem("carrot", 3, 64);
                giveItem("potato", 4, 64);
                giveItem("bone_meal", 5, 64);
                giveItem("composter", 6);
                giveItem("hay_block", 7, 64);
                giveItem("bread", 8, 64);
                break;
        }
    }

    private void giveItem(String item, int slot) {
        giveItem(item, slot, 1);
    }

    private void giveItem(String item, int slot, int count) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String command = String.format("item replace entity @s hotbar.%d with minecraft:%s %d", slot, item, count);
        mc.player.networkHandler.sendChatCommand(command);
    }

    private void clearHotbar() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        for (int i = 0; i < 9; i++) {
            String command = String.format("item replace entity @s hotbar.%d with minecraft:air", i);
            mc.player.networkHandler.sendChatCommand(command);
        }
    }
}
