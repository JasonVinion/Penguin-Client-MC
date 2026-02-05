package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.ui.screen.ListEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class InvCleaner extends Module {
    private int timer = 0;
    private ModeSetting mode = new ModeSetting("Mode", "Inclusive", "Inclusive", "Exclusive");
    private ModeSetting preset = new ModeSetting("Preset", "None", "None", "Miner", "PvP");
    private ActionSetting editList = new ActionSetting("Edit List...", () -> {
        MinecraftClient.getInstance().setScreen(new ListEditorScreen(null, this.itemList, list -> this.itemList = list));
    });

    private List<String> itemList = new ArrayList<>();
    private String lastPreset = "None";

    public InvCleaner() {
        super("InvCleaner", "Automatically drops unwanted items from inventory.", Category.PLAYER);
        addSetting(mode);
        addSetting(preset);
        addSetting(editList);

        // Default list
        itemList.add("minecraft:dirt");
        itemList.add("minecraft:cobblestone");
        itemList.add("minecraft:netherrack");
    }

    @Override
    public void onTick() {
        if (!preset.getMode().equals(lastPreset)) {
            lastPreset = preset.getMode();
            if (lastPreset.equals("Miner")) {
                itemList.clear();
                itemList.add("minecraft:dirt");
                itemList.add("minecraft:cobblestone");
                itemList.add("minecraft:granite");
                itemList.add("minecraft:diorite");
                itemList.add("minecraft:andesite");
                itemList.add("minecraft:gravel");
                itemList.add("minecraft:tuff");
                itemList.add("minecraft:deepslate");
            } else if (lastPreset.equals("PvP")) {
                itemList.clear();
                itemList.add("minecraft:rotten_flesh");
                itemList.add("minecraft:arrow");
                itemList.add("minecraft:wooden_sword");
                itemList.add("minecraft:stone_sword");
            }
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        timer++;
        if (timer < 2) return;
        timer = 0;

        for (int i = 9; i < 45; i++) {
            int slot = i;
            ItemStack s = mc.player.playerScreenHandler.getSlot(slot).getStack();

            if (s.isEmpty()) continue;

            String id = Registries.ITEM.getId(s.getItem()).toString();
            String simpleId = id.replace("minecraft:", "");
            boolean inList = itemList.contains(id) || itemList.contains(simpleId);

            boolean shouldThrow = false;

            if (mode.getMode().equals("Inclusive")) {
                if (inList) shouldThrow = true;
            } else {
                if (!inList) shouldThrow = true;
            }

            if (shouldThrow) {
                mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, slot, 1, SlotActionType.THROW, mc.player);
                return;
            }
        }
    }
}
