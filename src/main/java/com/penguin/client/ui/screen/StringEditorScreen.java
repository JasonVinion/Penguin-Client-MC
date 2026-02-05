package com.penguin.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class StringEditorScreen extends Screen {
    private final Screen parent;
    private final String currentText;
    private final Consumer<String> onSave;
    private TextFieldWidget inputField;

    public StringEditorScreen(Screen parent, String currentText, Consumer<String> onSave) {
        super(Text.of("Edit Text"));
        this.parent = parent;
        this.currentText = currentText;
        this.onSave = onSave;
    }

    @Override
    protected void init() {
        this.inputField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 60, 200, 20, Text.of("Input"));
        this.inputField.setText(currentText);
        this.inputField.setMaxLength(256);
        this.addDrawableChild(this.inputField);
        
        // Auto-focus the input field so user can type immediately
        this.setInitialFocus(this.inputField);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Save"), button -> {
            onSave.accept(inputField.getText());
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 50, 200, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.client.setScreen(parent);
            return true;
        }
        if (keyCode == 257) { // Enter
            onSave.accept(inputField.getText());
            this.client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
