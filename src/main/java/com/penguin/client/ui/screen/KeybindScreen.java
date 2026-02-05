package com.penguin.client.ui.screen;

import com.penguin.client.config.ConfigManager;
import com.penguin.client.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Screen for setting keybindings for modules.
 */
public class KeybindScreen extends Screen {
    private final Screen parent;
    private final Module module;
    private boolean waitingForKey = true;

    public KeybindScreen(Screen parent, Module module) {
        super(Text.of("Set Keybind"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Draw background box
        int boxWidth = 200;
        int boxHeight = 80;
        context.fill(centerX - boxWidth/2, centerY - boxHeight/2, 
                     centerX + boxWidth/2, centerY + boxHeight/2, 0xDD000000);
        context.drawBorder(centerX - boxWidth/2, centerY - boxHeight/2, 
                          boxWidth, boxHeight, 0xFF55FFFF);
        
        // Draw title
        String title = "Keybind: " + module.getName();
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawText(this.textRenderer, Text.of(title), 
                        centerX - titleWidth/2, centerY - 30, 0xFFFFFFFF, true);
        
        // Draw current binding
        String currentKey = module.getKey() == -1 ? "None" : getKeyName(module.getKey());
        String currentText = "Current: " + currentKey;
        int currentWidth = this.textRenderer.getWidth(currentText);
        context.drawText(this.textRenderer, Text.of(currentText), 
                        centerX - currentWidth/2, centerY - 10, 0xFFAAAAAA, false);
        
        // Draw instruction
        String instruction = waitingForKey ? "Press any key to bind..." : "Key bound!";
        int instructionWidth = this.textRenderer.getWidth(instruction);
        context.drawText(this.textRenderer, Text.of(instruction), 
                        centerX - instructionWidth/2, centerY + 10, 0xFF55FF55, false);
        
        // Draw cancel hint
        String hint = "ESC to cancel, DELETE to unbind";
        int hintWidth = this.textRenderer.getWidth(hint);
        context.drawText(this.textRenderer, Text.of(hint), 
                        centerX - hintWidth/2, centerY + 25, 0xFF888888, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!waitingForKey) {
            this.close();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        
        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            // Unbind
            module.setKey(-1);
            ConfigManager.INSTANCE.save();
            waitingForKey = false;
            return true;
        }
        
        // Set the keybind
        module.setKey(keyCode);
        ConfigManager.INSTANCE.save();
        waitingForKey = false;
        
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
    private String getKeyName(int keyCode) {
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null) {
            return name.toUpperCase();
        }
        
        // Handle special keys
        switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "L_SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "R_SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "L_CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "R_CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT: return "L_ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "R_ALT";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "CAPS";
            case GLFW.GLFW_KEY_F1: return "F1";
            case GLFW.GLFW_KEY_F2: return "F2";
            case GLFW.GLFW_KEY_F3: return "F3";
            case GLFW.GLFW_KEY_F4: return "F4";
            case GLFW.GLFW_KEY_F5: return "F5";
            case GLFW.GLFW_KEY_F6: return "F6";
            case GLFW.GLFW_KEY_F7: return "F7";
            case GLFW.GLFW_KEY_F8: return "F8";
            case GLFW.GLFW_KEY_F9: return "F9";
            case GLFW.GLFW_KEY_F10: return "F10";
            case GLFW.GLFW_KEY_F11: return "F11";
            case GLFW.GLFW_KEY_F12: return "F12";
            default: return "KEY_" + keyCode;
        }
    }
}
