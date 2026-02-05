package com.penguin.client.ui.screen;

import com.penguin.client.config.KeybindConfig;
import com.penguin.client.module.modules.ClientSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Screen for configuring mod navigation keybinds and UI mode.
 * Accessible from Minecraft settings or from ClientSettings module.
 */
public class ModKeybindSettingsScreen extends Screen {
    private final Screen parent;
    private List<KeybindEntry> entries;
    private int selectedEntry = -1;
    private boolean waitingForKey = false;
    private ButtonWidget uiModeButton;

    private static class KeybindEntry {
        String name;
        Supplier<Integer> getter;
        Consumer<Integer> setter;
        ButtonWidget button;

        KeybindEntry(String name, Supplier<Integer> getter, Consumer<Integer> setter) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
        }
    }

    public ModKeybindSettingsScreen(Screen parent) {
        super(Text.of("Penguin Client Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        entries = new ArrayList<>();

        KeybindConfig config = KeybindConfig.INSTANCE;

        // Add all keybind entries
        entries.add(new KeybindEntry("Menu Toggle", config::getMenuToggleKey, config::setMenuToggleKey));
        entries.add(new KeybindEntry("Navigate Up", config::getNavigateUpKey, config::setNavigateUpKey));
        entries.add(new KeybindEntry("Navigate Down", config::getNavigateDownKey, config::setNavigateDownKey));
        entries.add(new KeybindEntry("Navigate Left", config::getNavigateLeftKey, config::setNavigateLeftKey));
        entries.add(new KeybindEntry("Navigate Right", config::getNavigateRightKey, config::setNavigateRightKey));
        entries.add(new KeybindEntry("Select/Enter", config::getSelectKey, config::setSelectKey));
        entries.add(new KeybindEntry("Back", config::getBackKey, config::setBackKey));

        // Create buttons for each entry
        int buttonWidth = 100;
        int buttonHeight = 20;
        int startY = 50;
        int spacing = 28; // Increased spacing for better layout
        int columnWidth = 200;

        for (int i = 0; i < entries.size(); i++) {
            KeybindEntry entry = entries.get(i);
            int column = i % 2;
            int row = i / 2;
            int y = startY + row * spacing;
            int labelX = this.width / 2 - columnWidth + (column * columnWidth);
            final int index = i;

            entry.button = ButtonWidget.builder(
                Text.of(KeybindConfig.getKeyName(entry.getter.get())),
                button -> {
                    selectedEntry = index;
                    waitingForKey = true;
                    button.setMessage(Text.of("> ? <"));
                }
            ).dimensions(labelX + 90, y, buttonWidth, buttonHeight).build();

            this.addDrawableChild(entry.button);
        }

        // UI Mode toggle button - increased spacing from keybind entries
        int uiModeY = startY + ((entries.size() + 1) / 2) * spacing + 20;
        uiModeButton = ButtonWidget.builder(
            Text.of("UI Mode: " + getUIModeName()),
            button -> {
                toggleUIMode();
                button.setMessage(Text.of("UI Mode: " + getUIModeName()));
            }
        ).dimensions(this.width / 2 - 50, uiModeY, 100, buttonHeight).build();
        this.addDrawableChild(uiModeButton);

        // Reset to defaults button - positioned with more spacing from bottom
        this.addDrawableChild(ButtonWidget.builder(
            Text.of("Reset to Defaults"),
            button -> {
                KeybindConfig.INSTANCE.resetToDefaults();
                updateButtons();
            }
        ).dimensions(this.width / 2 - 100, this.height - 55, 95, 20).build());

        // Done button
        this.addDrawableChild(ButtonWidget.builder(
            Text.of("Done"),
            button -> this.close()
        ).dimensions(this.width / 2 + 5, this.height - 55, 95, 20).build());
    }

    private String getUIModeName() {
        if (ClientSettings.INSTANCE != null && ClientSettings.INSTANCE.isClickGUI()) {
            return "Click";
        }
        return "List";
    }

    private void toggleUIMode() {
        if (ClientSettings.INSTANCE != null) {
            ClientSettings.INSTANCE.cycleUIMode();
        }
    }

    private void updateButtons() {
        for (KeybindEntry entry : entries) {
            entry.button.setMessage(Text.of(KeybindConfig.getKeyName(entry.getter.get())));
        }
        if (uiModeButton != null) {
            uiModeButton.setMessage(Text.of("UI Mode: " + getUIModeName()));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);

        // Draw subtitle
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.of("Configure keybinds and UI settings for Penguin Client"), 
            this.width / 2, 30, 0xAAAAAA);

        // Draw labels for each keybind
        int startY = 50;
        int spacing = 28; // Increased spacing for better layout
        int columnWidth = 200;

        for (int i = 0; i < entries.size(); i++) {
            KeybindEntry entry = entries.get(i);
            int column = i % 2;
            int row = i / 2;
            int y = startY + row * spacing;
            int labelX = this.width / 2 - columnWidth + (column * columnWidth);

            // Draw label
            context.drawTextWithShadow(this.textRenderer, Text.of(entry.name + ":"), 
                labelX - 10, y + 6, 0xFFFFFF);
        }

        // Draw UI Mode label and description - increased spacing
        int uiModeY = startY + ((entries.size() + 1) / 2) * spacing + 20;
        context.drawTextWithShadow(this.textRenderer, Text.of("UI Mode:"), 
            this.width / 2 - 100, uiModeY + 6, 0xFFFFFF);
        
        // UI Mode description
        String uiModeDesc = getUIModeName().equals("Click") 
            ? "Click UI: Draggable panels with mouse interaction" 
            : "List UI: Keyboard-based navigation menu";
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.of(uiModeDesc), 
            this.width / 2, uiModeY + 28, 0x888888);

        // Draw waiting message if applicable
        if (waitingForKey && selectedEntry >= 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, 
                Text.of("Press a key to bind, ESC to cancel, DELETE to unbind"),
                this.width / 2, this.height - 80, 0xFFFF55);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKey && selectedEntry >= 0 && selectedEntry < entries.size()) {
            KeybindEntry entry = entries.get(selectedEntry);

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // Cancel
                waitingForKey = false;
                entry.button.setMessage(Text.of(KeybindConfig.getKeyName(entry.getter.get())));
                selectedEntry = -1;
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                // Unbind - set to -1
                entry.setter.accept(-1);
            } else {
                // Set the new key
                entry.setter.accept(keyCode);
            }

            waitingForKey = false;
            entry.button.setMessage(Text.of(KeybindConfig.getKeyName(entry.getter.get())));
            selectedEntry = -1;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
