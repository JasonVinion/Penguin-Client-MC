package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that gives the player full armor sets.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class GiveArmorSet extends Module {
    private final ModeSetting armorType = new ModeSetting("Armor Type", "Diamond",
            "Leather", "Iron", "Gold", "Diamond", "Netherite");
    
    private final ActionSetting equipArmor = new ActionSetting("Equip Armor", this::equipArmor);
    private final ActionSetting removeArmor = new ActionSetting("Remove Armor", this::removeArmor);

    public GiveArmorSet() {
        super("GiveArmorSet", "Gives player a full armor set. Requires OP/Creative.", Category.TESTING);
        addSetting(armorType);
        addSetting(equipArmor);
        addSetting(removeArmor);
    }

    @Override
    public void onEnable() {
        equipArmor();
        this.toggle();
    }

    private void equipArmor() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String material = armorType.getMode().toLowerCase();
        
        String helmetCmd = String.format("item replace entity @s armor.head with minecraft:%s_helmet", material);
        String chestCmd = String.format("item replace entity @s armor.chest with minecraft:%s_chestplate", material);
        String legsCmd = String.format("item replace entity @s armor.legs with minecraft:%s_leggings", material);
        String bootsCmd = String.format("item replace entity @s armor.feet with minecraft:%s_boots", material);
        
        mc.player.networkHandler.sendChatCommand(helmetCmd);
        mc.player.networkHandler.sendChatCommand(chestCmd);
        mc.player.networkHandler.sendChatCommand(legsCmd);
        mc.player.networkHandler.sendChatCommand(bootsCmd);
    }

    private void removeArmor() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("item replace entity @s armor.head with minecraft:air");
        mc.player.networkHandler.sendChatCommand("item replace entity @s armor.chest with minecraft:air");
        mc.player.networkHandler.sendChatCommand("item replace entity @s armor.legs with minecraft:air");
        mc.player.networkHandler.sendChatCommand("item replace entity @s armor.feet with minecraft:air");
    }
}
