package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ColorSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.ui.screen.ColorPickerScreen;
import com.penguin.client.ui.screen.ModKeybindSettingsScreen;
import net.minecraft.client.MinecraftClient;

/**
 * Global client settings module for colors, keybinds, etc.
 * UI Mode and Beta Tester settings are now separate modules.
 */
public class ClientSettings extends Module {
    public static ClientSettings INSTANCE;

    // Color Settings
    private final ColorSetting activeListColor = new ColorSetting("Active List Color", 0xFF55FF55);
    private final ColorSetting menuTitleColor = new ColorSetting("Menu Title Color", 0xFF55FFFF);
    private final ColorSetting menuBackgroundColor = new ColorSetting("Menu Background", 0xDD1a1a1a);
    private final ColorSetting selectedItemColor = new ColorSetting("Selected Item", 0x90000000);
    private final ColorSetting unselectedItemColor = new ColorSetting("Unselected Item", 0x90222222);
    private final ColorSetting enabledTextColor = new ColorSetting("Enabled Text", 0xFF55FF55);
    private final ColorSetting disabledTextColor = new ColorSetting("Disabled Text", 0xFFFF5555);
    private final ColorSetting normalTextColor = new ColorSetting("Normal Text", 0xFFAAAAAA);
    private final ColorSetting highlightedTextColor = new ColorSetting("Highlighted Text", 0xFFFFFFFF);

    // Rainbow settings
    private final BooleanSetting rainbowMode = new BooleanSetting("Rainbow Mode", false);
    private final NumberSetting rainbowSpeed = new NumberSetting("Rainbow Speed", 1.0, 0.1, 5.0, 0.1);

    // Global settings
    public final BooleanSetting disableEverythingOnJoin = new BooleanSetting("Disable everything on join world", false);

    // Action buttons
    private final ActionSetting openColorPicker = new ActionSetting("Edit Colors", () -> {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.setScreen(new ColorPickerScreen(null, INSTANCE));
        }
    });

    private final ActionSetting openKeybindSettings = new ActionSetting("Mod Keybinds", () -> {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.setScreen(new ModKeybindSettingsScreen(null));
        }
    });

    public ClientSettings() {
        super("ClientSettings", "Global settings for colors and keybinds.", Category.SETTINGS);
        INSTANCE = this;

        // Add settings in order
        addSetting(rainbowMode);
        addSetting(rainbowSpeed);
        addSetting(disableEverythingOnJoin);
        addSetting(openColorPicker);
        addSetting(openKeybindSettings);

        // Color settings (hidden by default, shown in color picker)
        activeListColor.setVisible(false);
        menuTitleColor.setVisible(false);
        menuBackgroundColor.setVisible(false);
        selectedItemColor.setVisible(false);
        unselectedItemColor.setVisible(false);
        enabledTextColor.setVisible(false);
        disabledTextColor.setVisible(false);
        normalTextColor.setVisible(false);
        highlightedTextColor.setVisible(false);

        addSetting(activeListColor);
        addSetting(menuTitleColor);
        addSetting(menuBackgroundColor);
        addSetting(selectedItemColor);
        addSetting(unselectedItemColor);
        addSetting(enabledTextColor);
        addSetting(disabledTextColor);
        addSetting(normalTextColor);
        addSetting(highlightedTextColor);
    }

    // Getters for UI mode - delegate to UIMode module
    public boolean isClickGUI() {
        return UIMode.INSTANCE != null && UIMode.INSTANCE.isClickGUI();
    }

    public boolean isListGUI() {
        return UIMode.INSTANCE == null || UIMode.INSTANCE.isListGUI();
    }

    /**
     * Cycles the UI mode between List and Click.
     */
    public void cycleUIMode() {
        if (UIMode.INSTANCE != null) {
            UIMode.INSTANCE.cycleUIMode();
        }
    }

    /**
     * Returns true if beta tester mode is enabled.
     * Beta tester mode enables the Testing category with developer/testing modules.
     * This is for testing purposes only and is recommended to keep off for stability.
     */
    public boolean isBetaTesterMode() {
        return BetaTesterMode.INSTANCE != null && BetaTesterMode.INSTANCE.isBetaTesterEnabled();
    }

    public int getActiveListColor(int itemIndex) {
        if (rainbowMode.isEnabled()) {
            activeListColor.setRainbowSpeed((float) rainbowSpeed.getValue());
            return activeListColor.getRainbowColor(itemIndex);
        }
        return activeListColor.getColor();
    }

    public int getMenuTitleColor() {
        if (rainbowMode.isEnabled()) {
            menuTitleColor.setRainbowSpeed((float) rainbowSpeed.getValue());
            return menuTitleColor.getRainbowColor();
        }
        return menuTitleColor.getColor();
    }

    public int getMenuBackgroundColor() {
        return menuBackgroundColor.getColor();
    }

    public int getSelectedItemColor() {
        return selectedItemColor.getColor();
    }

    public int getUnselectedItemColor() {
        return unselectedItemColor.getColor();
    }

    public int getEnabledTextColor() {
        if (rainbowMode.isEnabled()) {
            enabledTextColor.setRainbowSpeed((float) rainbowSpeed.getValue());
            return enabledTextColor.getRainbowColor();
        }
        return enabledTextColor.getColor();
    }

    public int getDisabledTextColor() {
        return disabledTextColor.getColor();
    }

    public int getNormalTextColor() {
        return normalTextColor.getColor();
    }

    public int getHighlightedTextColor() {
        return highlightedTextColor.getColor();
    }

    public boolean isRainbowMode() {
        return rainbowMode.isEnabled();
    }

    public double getRainbowSpeed() {
        return rainbowSpeed.getValue();
    }

    // Direct access to color settings for the color picker
    public ColorSetting getActiveListColorSetting() {
        return activeListColor;
    }

    public ColorSetting getMenuTitleColorSetting() {
        return menuTitleColor;
    }

    public ColorSetting getMenuBackgroundColorSetting() {
        return menuBackgroundColor;
    }

    public ColorSetting getSelectedItemColorSetting() {
        return selectedItemColor;
    }

    public ColorSetting getUnselectedItemColorSetting() {
        return unselectedItemColor;
    }

    public ColorSetting getEnabledTextColorSetting() {
        return enabledTextColor;
    }

    public ColorSetting getDisabledTextColorSetting() {
        return disabledTextColor;
    }

    public ColorSetting getNormalTextColorSetting() {
        return normalTextColor;
    }

    public ColorSetting getHighlightedTextColorSetting() {
        return highlightedTextColor;
    }
}
