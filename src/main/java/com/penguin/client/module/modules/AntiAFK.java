package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

import java.util.Random;

public class AntiAFK extends Module {
    private BooleanSetting rotate = new BooleanSetting("Rotate", true);
    private BooleanSetting jump = new BooleanSetting("Jump", false);
    private BooleanSetting walk = new BooleanSetting("Walk", false);
    private BooleanSetting punch = new BooleanSetting("Punch", false);
    private BooleanSetting chat = new BooleanSetting("Chat", false);
    private NumberSetting chatDelay = new NumberSetting("Chat Delay", 60.0, 10.0, 300.0, 5.0);

    private int timer = 0;
    private int chatTimer = 0;
    private Random random = new Random();
    
    private static final String[] CHAT_MESSAGES = {
        ".", "..", "ok", "yes", "no", "yeah", "yep", "nah", "k", "lol",
        "hmm", "brb", "afk", "here", "hi", "hey"
    };

    public AntiAFK() {
        super("AntiAFK", "Prevents being kicked for AFK. Performs random actions like rotating, jumping, walking, punching, or sending chat messages.", Category.PLAYER);
        addSetting(rotate);
        addSetting(jump);
        addSetting(walk);
        addSetting(punch);
        addSetting(chat);
        addSetting(chatDelay);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        timer++;
        if (timer < 100 + random.nextInt(100)) return; // Every 5-10 seconds
        timer = 0;

        if (rotate.isEnabled()) {
             mc.player.setYaw(mc.player.getYaw() + random.nextFloat() * 10 - 5);
             mc.player.setPitch(mc.player.getPitch() + random.nextFloat() * 10 - 5);
        }
        if (jump.isEnabled()) {
             if (mc.player.isOnGround()) mc.player.jump();
        }
        if (walk.isEnabled()) {
             double angle = Math.toRadians(mc.player.getYaw());
             mc.player.setVelocity(mc.player.getVelocity().add(-Math.sin(angle) * 0.1, 0, Math.cos(angle) * 0.1));
        }
        if (punch.isEnabled()) {
             mc.player.swingHand(Hand.MAIN_HAND);
        }
        
        // Handle chat separately with its own timer
        if (chat.isEnabled()) {
            chatTimer++;
            int chatDelayTicks = (int)(chatDelay.getValue() * 20); // Convert seconds to ticks
            if (chatTimer >= chatDelayTicks) {
                chatTimer = 0;
                String message = CHAT_MESSAGES[random.nextInt(CHAT_MESSAGES.length)];
                mc.player.networkHandler.sendChatMessage(message);
            }
        }
    }
    
    @Override
    public void onEnable() {
        timer = 0;
        chatTimer = 0;
    }
}
