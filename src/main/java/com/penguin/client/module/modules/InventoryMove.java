package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class InventoryMove extends Module {
    private BooleanSetting rotate = new BooleanSetting("Rotate", true);

    public InventoryMove() {
        super("InventoryMove", "Allows moving while in inventory.", Category.MOVEMENT);
        addSetting(rotate);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen == null) return;

        // Chat screen usually allows movement anyway? Or we should avoid moving in chat.
        if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen) return;

        if (rotate.isEnabled()) {
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            float speed = 5.0f; // Rotation speed

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) yaw -= speed;
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) yaw += speed;
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) pitch -= speed;
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) pitch += speed;

            // Constrain pitch
            if (pitch > 90) pitch = 90;
            if (pitch < -90) pitch = -90;

            mc.player.setYaw(yaw);
            mc.player.setPitch(pitch);
        }
    }
}
