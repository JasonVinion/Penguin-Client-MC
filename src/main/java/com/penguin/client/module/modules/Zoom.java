package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {
    public static NumberSetting amount = new NumberSetting("Amount", 2.0, 1.0, 10.0, 0.5);
    public static BooleanSetting smoothZoom = new BooleanSetting("Smooth Zoom", true);
    public static BooleanSetting scrollControl = new BooleanSetting("Scroll Control", false);
    public static BooleanSetting keyControl = new BooleanSetting("Key Control (+/-)", true);
    public static NumberSetting smoothness = new NumberSetting("Smoothness", 0.5, 0.1, 1.0, 0.1);
    public static NumberSetting scrollSensitivity = new NumberSetting("Scroll Sensitivity", 0.5, 0.1, 2.0, 0.1);
    
    private static Zoom INSTANCE;
    private static double currentZoom = 1.0;
    private boolean plusKeyPressed = false;
    private boolean minusKeyPressed = false;

    public Zoom() {
        super("Zoom", "Decreases FOV to zoom in like a spyglass. Use scroll wheel or +/- keys to adjust.", Category.RENDER);
        addSetting(amount);
        addSetting(smoothZoom);
        addSetting(scrollControl);
        addSetting(keyControl);
        addSetting(smoothness);
        addSetting(scrollSensitivity);
        INSTANCE = this;
    }
    
    public static boolean isEnabledStatic() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
    
    public static double getZoomAmount() {
        if (INSTANCE == null) return 1.0;
        
        double targetZoom = INSTANCE.amount.getValue();
        
        if (INSTANCE.smoothZoom.isEnabled()) {
            double smooth = INSTANCE.smoothness.getValue();
            currentZoom = currentZoom + (targetZoom - currentZoom) * smooth;
            return currentZoom;
        }
        
        return targetZoom;
    }
    
    public static void adjustZoom(double delta) {
        if (INSTANCE != null && INSTANCE.scrollControl.isEnabled()) {
            double currentAmount = INSTANCE.amount.getValue();
            double sensitivity = INSTANCE.scrollSensitivity.getValue();
            double newAmount = currentAmount + delta * sensitivity;
            // Clamp to valid range
            if (newAmount < 1.0) newAmount = 1.0;
            if (newAmount > 10.0) newAmount = 10.0;
            // Use setValue directly for efficiency
            INSTANCE.amount.setValue(newAmount);
        }
    }
    
    @Override
    public void onTick() {
        if (!isEnabled()) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;
        
        // Handle keyboard zoom control with + and - keys (with key press tracking to avoid continuous triggering)
        if (keyControl.isEnabled()) {
            long window = mc.getWindow().getHandle();
            
            // Check for + key (equals/plus key and numpad plus)
            boolean plusPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_EQUAL) == GLFW.GLFW_PRESS || 
                                  GLFW.glfwGetKey(window, GLFW.GLFW_KEY_KP_ADD) == GLFW.GLFW_PRESS;
            if (plusPressed && !plusKeyPressed) {
                adjustZoomByKey(0.5);
            }
            plusKeyPressed = plusPressed;
            
            // Check for - key (minus key and numpad minus)
            boolean minusPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_MINUS) == GLFW.GLFW_PRESS || 
                                   GLFW.glfwGetKey(window, GLFW.GLFW_KEY_KP_SUBTRACT) == GLFW.GLFW_PRESS;
            if (minusPressed && !minusKeyPressed) {
                adjustZoomByKey(-0.5);
            }
            minusKeyPressed = minusPressed;
        }
    }
    
    private static void adjustZoomByKey(double delta) {
        if (INSTANCE != null) {
            double currentAmount = INSTANCE.amount.getValue();
            double newAmount = currentAmount + delta;
            // Clamp to valid range
            if (newAmount < 1.0) newAmount = 1.0;
            if (newAmount > 10.0) newAmount = 10.0;
            INSTANCE.amount.setValue(newAmount);
        }
    }
    
    @Override
    public void onEnable() {
        currentZoom = 1.0;
        plusKeyPressed = false;
        minusKeyPressed = false;
    }
    
    @Override
    public void onDisable() {
        currentZoom = 1.0;
    }
}
