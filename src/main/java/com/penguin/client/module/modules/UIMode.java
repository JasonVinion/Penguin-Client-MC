package com.penguin.client.module.modules;

import com.penguin.client.config.ConfigManager;
import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.ui.MenuHUD;
import com.penguin.client.ui.screen.ClickGUIScreen;
import net.minecraft.client.MinecraftClient;

/**
 * UI Mode module - allows switching between List and Click UI modes.
 * Acts as a button: when toggled, it cycles the UI mode, opens the appropriate
 * UI (List HUD or Click GUI screen), saves config, and immediately disables itself.
 */
public class UIMode extends Module {
    public static UIMode INSTANCE;

    private final ModeSetting uiMode = new ModeSetting("Mode", "List", "List", "Click");
    
    // Flag to prevent re-entrancy during toggle
    private boolean isToggling = false;

    public UIMode() {
        super("UIMode", 
              "Switch between List and Click UI modes. Toggle to switch modes.",
              Category.SETTINGS);
        INSTANCE = this;
        addSetting(uiMode);
    }

    /**
     * Returns true if Click GUI mode is active.
     */
    public boolean isClickGUI() {
        return uiMode.is("Click");
    }

    /**
     * Returns true if List GUI mode is active.
     */
    public boolean isListGUI() {
        return uiMode.is("List");
    }

    /**
     * Cycles the UI mode between List and Click.
     */
    public void cycleUIMode() {
        uiMode.cycle();
    }

    @Override
    public void onEnable() {
        // Prevent re-entrancy from the self-toggle at the end
        if (isToggling) return;
        isToggling = true;
        
        try {
            // Cycle to the next UI mode
            uiMode.cycle();
            
            // Open the appropriate UI
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
                if (isClickGUI()) {
                    MenuHUD.setVisible(false);
                    mc.setScreen(ClickGUIScreen.getInstance());
                } else {
                    mc.setScreen(null);
                    MenuHUD.setVisible(true);
                }
            }
            
            // Save the config after mode change
            ConfigManager.INSTANCE.save();
        } finally {
            isToggling = false;
            // Immediately disable so this acts as a momentary button
            this.toggle();
        }
    }

    @Override
    public void onDisable() {
        // Nothing special on disable - this module acts as a button
    }
}
