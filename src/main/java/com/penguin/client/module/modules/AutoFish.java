package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoFish extends Module {
    private int timer = 0;
    public static AutoFish INSTANCE;
    public boolean caught = false;
    private int recastTimer = 0;

    public AutoFish() {
        super("AutoFish", "Automatically reels in and recasts fishing rod when fish bites.", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (recastTimer > 0) {
            recastTimer--;
            if (recastTimer == 0) {
                 mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
            return;
        }

        if (mc.player.getMainHandStack().getItem() == Items.FISHING_ROD) {
            if (mc.player.fishHook == null) {
                 timer++;
                 if (timer > 20) {
                     mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                     timer = 0;
                 }
            } else {
                if (caught) {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    caught = false;
                    recastTimer = 15;
                }
            }
        }
    }
}
