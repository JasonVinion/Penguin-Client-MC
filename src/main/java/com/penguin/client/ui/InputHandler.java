package com.penguin.client.ui;

import com.penguin.client.config.KeybindConfig;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.ClientSettings;
import com.penguin.client.ui.screen.ClickGUIScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class InputHandler {

    private static boolean menuTogglePressed = false;
    private static boolean[] keyStates = new boolean[512]; // Track key states for keybinds

    // Dynamic key state tracking (updates based on KeybindConfig)
    private static DynamicKeyState upKey = new DynamicKeyState(() -> KeybindConfig.INSTANCE.getNavigateUpKey());
    private static DynamicKeyState downKey = new DynamicKeyState(() -> KeybindConfig.INSTANCE.getNavigateDownKey());
    private static DynamicKeyState leftKey = new DynamicKeyState(() -> KeybindConfig.INSTANCE.getNavigateLeftKey());
    private static DynamicKeyState rightKey = new DynamicKeyState(() -> KeybindConfig.INSTANCE.getNavigateRightKey());
    private static DynamicKeyState enterKey = new DynamicKeyState(() -> KeybindConfig.INSTANCE.getSelectKey());
    private static DynamicKeyState backspaceKey = new DynamicKeyState(() -> KeybindConfig.INSTANCE.getBackKey());

    private static class DynamicKeyState {
        java.util.function.Supplier<Integer> keySupplier;
        boolean pressed;
        long lastPressTime;
        long lastRepeatTime;

        DynamicKeyState(java.util.function.Supplier<Integer> keySupplier) {
            this.keySupplier = keySupplier;
        }

        void update(long window) {
            int key = keySupplier.get();
            if (key < 0) return;

            boolean isDown = InputUtil.isKeyPressed(window, key);
            long currentTime = System.currentTimeMillis();

            if (isDown) {
                if (!pressed) {
                    // First press
                    pressed = true;
                    lastPressTime = currentTime;
                    lastRepeatTime = currentTime;
                    MenuHUD.handleInput(key);
                } else {
                    // Held down
                    long timeSincePress = currentTime - lastPressTime;
                    long timeSinceRepeat = currentTime - lastRepeatTime;

                    // Initial delay of 400ms, then repeat every 50ms
                    if (timeSincePress > 400 && timeSinceRepeat > 50) {
                        lastRepeatTime = currentTime;
                        MenuHUD.handleInput(key);
                    }
                }
            } else {
                pressed = false;
            }
        }
    }

    public static void update() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return;

        // If a screen is open, do not process HUD inputs (except toggle if needed, but usually we want to avoid typing conflicts)
        if (mc.currentScreen != null) return;

        long window = mc.getWindow().getHandle();

        // Toggle visibility using configurable key (default: Insert)
        int menuToggleKey = KeybindConfig.INSTANCE.getMenuToggleKey();
        boolean toggleKeyDown = menuToggleKey >= 0 && InputUtil.isKeyPressed(window, menuToggleKey);
        if (toggleKeyDown && !menuTogglePressed) {
            // Check if Click GUI mode is enabled
            if (ClientSettings.INSTANCE != null && ClientSettings.INSTANCE.isClickGUI()) {
                // Close List Mode if it's open when switching to Click Mode
                if (MenuHUD.isVisible()) {
                    MenuHUD.setVisible(false);
                }
                // Toggle Click GUI screen (open if not open, close if already open)
                if (mc.currentScreen instanceof ClickGUIScreen) {
                    mc.setScreen(null);
                } else {
                    mc.setScreen(ClickGUIScreen.getInstance());
                }
            } else {
                // Toggle List GUI
                MenuHUD.toggleVisibility();
            }
            menuTogglePressed = true;
        } else if (!toggleKeyDown) {
            menuTogglePressed = false;
        }

        if (MenuHUD.isVisible()) {
            upKey.update(window);
            downKey.update(window);
            leftKey.update(window);
            rightKey.update(window);
            enterKey.update(window);
            backspaceKey.update(window);
        }
        
        // Handle module keybinds - optimized to only check keys that are actually bound
        // First pass: update key states only for bound keys and track which were just pressed
        // This preserves the ability for multiple modules to share the same keybind
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            int key = module.getKey();
            if (key > 0 && key < keyStates.length) {
                boolean isPressed = InputUtil.isKeyPressed(window, key);
                boolean wasPressed = keyStates[key];
                
                // Toggle on key down (not held)
                if (isPressed && !wasPressed) {
                    module.toggle();
                }
            }
        }
        
        // Second pass: update key states after all modules have been processed
        // This ensures multiple modules on the same key all toggle together
        for (Module module : ModuleManager.INSTANCE.getModules()) {
            int key = module.getKey();
            if (key > 0 && key < keyStates.length) {
                keyStates[key] = InputUtil.isKeyPressed(window, key);
            }
        }
    }
}
