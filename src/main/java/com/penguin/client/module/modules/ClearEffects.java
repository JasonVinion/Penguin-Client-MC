package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that clears various effects and status.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class ClearEffects extends Module {
    private final BooleanSetting clearInventory = new BooleanSetting("Clear Inventory", false);
    
    private final ActionSetting clearAllEffects = new ActionSetting("Clear All Effects", this::clearAllEffects);
    private final ActionSetting clearBadEffects = new ActionSetting("Clear Bad Effects", this::clearBadEffects);
    private final ActionSetting clearInventoryAction = new ActionSetting("Clear Inventory", this::clearInventory);
    private final ActionSetting clearXP = new ActionSetting("Clear XP", this::clearXP);

    public ClearEffects() {
        super("ClearEffects", "Clears effects, inventory, and XP. Requires OP/Creative.", Category.TESTING);
        addSetting(clearInventory);
        addSetting(clearAllEffects);
        addSetting(clearBadEffects);
        addSetting(clearInventoryAction);
        addSetting(clearXP);
    }

    @Override
    public void onEnable() {
        clearAllEffects();
        if (clearInventory.isEnabled()) {
            clearInventory();
        }
        this.toggle();
    }

    private void clearAllEffects() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("effect clear @s");
    }

    private void clearBadEffects() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Clear common negative effects
        String[] badEffects = {
            "poison", "wither", "slowness", "mining_fatigue", "nausea",
            "blindness", "hunger", "weakness", "levitation", "bad_omen"
        };
        
        for (String effect : badEffects) {
            mc.player.networkHandler.sendChatCommand("effect clear @s minecraft:" + effect);
        }
    }

    private void clearInventory() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("clear @s");
    }

    private void clearXP() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("xp set @s 0 levels");
        mc.player.networkHandler.sendChatCommand("xp set @s 0 points");
    }
}
