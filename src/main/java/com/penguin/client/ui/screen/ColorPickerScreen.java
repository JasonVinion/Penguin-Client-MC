package com.penguin.client.ui.screen;

import com.penguin.client.config.ConfigManager;
import com.penguin.client.module.modules.ClientSettings;
import com.penguin.client.settings.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for picking and editing colors.
 */
public class ColorPickerScreen extends Screen {
    private final Screen parent;
    private final ClientSettings settings;
    private final ColorSetting singleSetting;
    private List<ColorEntry> colorEntries;
    private int selectedEntry = 0;
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE = 8;

    private TextFieldWidget hexInput;

    private static class ColorEntry {
        String name;
        ColorSetting setting;

        ColorEntry(String name, ColorSetting setting) {
            this.name = name;
            this.setting = setting;
        }
    }

    public ColorPickerScreen(Screen parent, ClientSettings settings) {
        super(Text.of("Color Settings"));
        this.parent = parent;
        this.settings = settings;
        this.singleSetting = null;
    }

    public ColorPickerScreen(Screen parent, ColorSetting setting) {
        super(Text.of("Edit Color"));
        this.parent = parent;
        this.settings = null;
        this.singleSetting = setting;
    }

    @Override
    protected void init() {
        colorEntries = new ArrayList<>();

        if (singleSetting != null) {
            colorEntries.add(new ColorEntry(singleSetting.getName(), singleSetting));
        } else if (settings != null) {
            // Add all color settings
            colorEntries.add(new ColorEntry("Active Mod List", settings.getActiveListColorSetting()));
            colorEntries.add(new ColorEntry("Menu Title", settings.getMenuTitleColorSetting()));
            colorEntries.add(new ColorEntry("Menu Background", settings.getMenuBackgroundColorSetting()));
            colorEntries.add(new ColorEntry("Selected Item", settings.getSelectedItemColorSetting()));
            colorEntries.add(new ColorEntry("Unselected Item", settings.getUnselectedItemColorSetting()));
            colorEntries.add(new ColorEntry("Enabled Text", settings.getEnabledTextColorSetting()));
            colorEntries.add(new ColorEntry("Disabled Text", settings.getDisabledTextColorSetting()));
            colorEntries.add(new ColorEntry("Normal Text", settings.getNormalTextColorSetting()));
            colorEntries.add(new ColorEntry("Highlighted Text", settings.getHighlightedTextColorSetting()));
        }

        // Hex input field
        hexInput = new TextFieldWidget(this.textRenderer, this.width / 2 + 30, this.height - 80, 80, 20, Text.of("Hex"));
        hexInput.setMaxLength(9);
        updateHexFromSelected();
        this.addDrawableChild(hexInput);

        // RGB sliders represented as buttons for simplicity
        int buttonY = this.height - 110;

        // Red slider buttons
        this.addDrawableChild(ButtonWidget.builder(Text.of("-"), button -> {
            adjustColor(-16, 0, 0);
        }).dimensions(this.width / 2 - 100, buttonY, 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("+"), button -> {
            adjustColor(16, 0, 0);
        }).dimensions(this.width / 2 - 30, buttonY, 20, 20).build());

        // Green slider buttons
        this.addDrawableChild(ButtonWidget.builder(Text.of("-"), button -> {
            adjustColor(0, -16, 0);
        }).dimensions(this.width / 2 + 10, buttonY, 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("+"), button -> {
            adjustColor(0, 16, 0);
        }).dimensions(this.width / 2 + 80, buttonY, 20, 20).build());

        // Blue slider buttons
        this.addDrawableChild(ButtonWidget.builder(Text.of("-"), button -> {
            adjustColor(0, 0, -16);
        }).dimensions(this.width / 2 + 120, buttonY, 20, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("+"), button -> {
            adjustColor(0, 0, 16);
        }).dimensions(this.width / 2 + 190, buttonY, 20, 20).build());

        // Apply hex button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Apply"), button -> {
            applyHexInput();
        }).dimensions(this.width / 2 + 115, this.height - 80, 50, 20).build());

        // Rainbow toggle
        this.addDrawableChild(ButtonWidget.builder(Text.of("Rainbow: " + (isRainbowEnabled() ? "ON" : "OFF")), button -> {
            toggleRainbow();
            button.setMessage(Text.of("Rainbow: " + (isRainbowEnabled() ? "ON" : "OFF")));
        }).dimensions(this.width / 2 - 50, this.height - 55, 100, 20).build());

        // Done button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Done"), button -> {
            ConfigManager.INSTANCE.save();
            this.close();
        }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    private void adjustColor(int dr, int dg, int db) {
        if (selectedEntry >= 0 && selectedEntry < colorEntries.size()) {
            ColorSetting setting = colorEntries.get(selectedEntry).setting;
            int r = Math.max(0, Math.min(255, setting.getRed() + dr));
            int g = Math.max(0, Math.min(255, setting.getGreen() + dg));
            int b = Math.max(0, Math.min(255, setting.getBlue() + db));
            setting.setRed(r);
            setting.setGreen(g);
            setting.setBlue(b);
            updateHexFromSelected();
        }
    }

    private void applyHexInput() {
        if (selectedEntry >= 0 && selectedEntry < colorEntries.size()) {
            ColorSetting setting = colorEntries.get(selectedEntry).setting;
            setting.setFromHexString(hexInput.getText());
            updateHexFromSelected();
        }
    }

    private void updateHexFromSelected() {
        if (selectedEntry >= 0 && selectedEntry < colorEntries.size()) {
            ColorSetting setting = colorEntries.get(selectedEntry).setting;
            hexInput.setText(setting.getHexString());
        }
    }

    private boolean isRainbowEnabled() {
        if (selectedEntry >= 0 && selectedEntry < colorEntries.size()) {
            return colorEntries.get(selectedEntry).setting.isRainbow();
        }
        return false;
    }

    private void toggleRainbow() {
        if (selectedEntry >= 0 && selectedEntry < colorEntries.size()) {
            ColorSetting setting = colorEntries.get(selectedEntry).setting;
            setting.toggleRainbow();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        // Draw color entries list
        int listX = 30;
        int listY = 30;
        int entryHeight = 22;
        int listWidth = 150;

        for (int i = scrollOffset; i < Math.min(colorEntries.size(), scrollOffset + MAX_VISIBLE); i++) {
            ColorEntry entry = colorEntries.get(i);
            int y = listY + (i - scrollOffset) * entryHeight;

            // Background
            int bgColor = (i == selectedEntry) ? 0x90000000 : 0x60222222;
            context.fill(listX, y, listX + listWidth, y + entryHeight - 2, bgColor);

            // Color preview
            context.fill(listX + 2, y + 2, listX + 18, y + entryHeight - 4, entry.setting.getStaticColor());
            context.drawBorder(listX + 2, y + 2, 16, entryHeight - 6, 0xFFFFFFFF);

            // Name
            int textColor = (i == selectedEntry) ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.drawTextWithShadow(this.textRenderer, Text.of(entry.name), listX + 22, y + 6, textColor);
        }

        // Draw scroll indicators
        if (scrollOffset > 0) {
            context.drawTextWithShadow(this.textRenderer, Text.of("^"), listX + listWidth / 2, listY - 10, 0xFFFFFF55);
        }
        if (scrollOffset + MAX_VISIBLE < colorEntries.size()) {
            context.drawTextWithShadow(this.textRenderer, Text.of("v"), listX + listWidth / 2, listY + MAX_VISIBLE * entryHeight + 2, 0xFFFFFF55);
        }

        // Draw selected color preview (large)
        if (selectedEntry >= 0 && selectedEntry < colorEntries.size()) {
            ColorEntry entry = colorEntries.get(selectedEntry);
            int previewX = this.width / 2;
            int previewY = 50;
            int previewSize = 60;

            context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, entry.setting.getStaticColor());
            context.drawBorder(previewX, previewY, previewSize, previewSize, 0xFFFFFFFF);

            // Draw color name
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of(entry.name), 
                previewX + previewSize / 2, previewY + previewSize + 5, 0xFFFFFF);

            // Draw RGB values
            int rgbY = previewY + previewSize + 20;
            context.drawTextWithShadow(this.textRenderer, 
                Text.of("R: " + entry.setting.getRed()), previewX, rgbY, 0xFFFF5555);
            context.drawTextWithShadow(this.textRenderer, 
                Text.of("G: " + entry.setting.getGreen()), previewX + 50, rgbY, 0xFF55FF55);
            context.drawTextWithShadow(this.textRenderer, 
                Text.of("B: " + entry.setting.getBlue()), previewX + 100, rgbY, 0xFF5555FF);

            // Draw RGB slider labels
            int buttonY = this.height - 110;
            context.drawTextWithShadow(this.textRenderer, Text.of("R"), this.width / 2 - 65, buttonY + 6, 0xFFFF5555);
            context.drawTextWithShadow(this.textRenderer, Text.of("G"), this.width / 2 + 45, buttonY + 6, 0xFF55FF55);
            context.drawTextWithShadow(this.textRenderer, Text.of("B"), this.width / 2 + 155, buttonY + 6, 0xFF5555FF);
        }

        // Draw hex label
        context.drawTextWithShadow(this.textRenderer, Text.of("Hex:"), this.width / 2 - 10, this.height - 75, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if clicking on color list
        int listX = 30;
        int listY = 30;
        int entryHeight = 22;
        int listWidth = 150;

        if (mouseX >= listX && mouseX <= listX + listWidth) {
            for (int i = scrollOffset; i < Math.min(colorEntries.size(), scrollOffset + MAX_VISIBLE); i++) {
                int y = listY + (i - scrollOffset) * entryHeight;
                if (mouseY >= y && mouseY < y + entryHeight) {
                    selectedEntry = i;
                    updateHexFromSelected();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int listX = 30;
        int listWidth = 150;

        if (mouseX >= listX && mouseX <= listX + listWidth) {
            if (amount > 0 && scrollOffset > 0) {
                scrollOffset--;
            } else if (amount < 0 && scrollOffset + MAX_VISIBLE < colorEntries.size()) {
                scrollOffset++;
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            ConfigManager.INSTANCE.save();
            this.close();
            return true;
        }

        // Arrow keys for list navigation
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            if (selectedEntry < colorEntries.size() - 1) {
                selectedEntry++;
                if (selectedEntry >= scrollOffset + MAX_VISIBLE) {
                    scrollOffset++;
                }
                updateHexFromSelected();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            if (selectedEntry > 0) {
                selectedEntry--;
                if (selectedEntry < scrollOffset) {
                    scrollOffset--;
                }
                updateHexFromSelected();
            }
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
