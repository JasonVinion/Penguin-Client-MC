package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Information module for beta testers explaining the Testing category.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 * When enabled, displays testing information on screen.
 */
public class BetaTesterInfo extends Module {

    public BetaTesterInfo() {
        super("BetaTesterInfo", 
              "BETA TESTING MODE: Displays testing information on screen. " +
              "These modules require OP/Creative mode on a server you own. " +
              "Features here are for development testing only.", 
              Category.TESTING);
    }

    @Override
    public void onRender(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        
        // Display beta tester info on screen
        int x = 5;
        int y = 5;
        int lineHeight = 12;
        
        context.drawTextWithShadow(mc.textRenderer, Text.of("§b[Beta Tester Mode Active]"), x, y, 0x55FFFF);
        y += lineHeight;
        context.drawTextWithShadow(mc.textRenderer, Text.of("§7Testing modules require OP/Creative"), x, y, 0xAAAAAA);
        y += lineHeight;
        context.drawTextWithShadow(mc.textRenderer, Text.of("§7Use on servers you own for testing"), x, y, 0xAAAAAA);
    }
}
