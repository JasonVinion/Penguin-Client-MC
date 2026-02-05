package com.penguin.client.ui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListEditorScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget inputField;
    private List<String> list;
    private final Consumer<List<String>> onSave;

    public ListEditorScreen(Screen parent, List<String> currentList, Consumer<List<String>> onSave) {
        super(Text.of("Edit List"));
        this.parent = parent;
        this.onSave = onSave;
        this.list = new ArrayList<>(currentList);
    }

    @Override
    protected void init() {
        this.inputField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 40, 200, 20, Text.of("Item Name"));
        this.inputField.setMaxLength(256);
        this.addDrawableChild(this.inputField);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Add"), button -> addItem()).dimensions(this.width / 2 + 110, 40, 50, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Remove"), button -> {
            String name = inputField.getText();
            list.remove(name);
            inputField.setText("");
        }).dimensions(this.width / 2 + 110, 65, 80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Save & Exit"), button -> {
            onSave.accept(list);
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    private void addItem() {
        String name = inputField.getText();
        Identifier id = Identifier.tryParse(name);
        if (id == null) id = Identifier.tryParse("minecraft:" + name);

        if (id != null) {
            String validName = id.toString();
            if (!list.contains(validName)) list.add(validName);
            inputField.setText("");
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int y = 70;
        for (String s : list) {
            if (mouseX >= this.width / 2 - 90 && mouseX <= this.width / 2 + 90 && mouseY >= y && mouseY < y + 10) {
                inputField.setText(s);
                return true;
            }
            y += 10;
            if (y > this.height - 40) break;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
             onSave.accept(list);
             this.client.setScreen(parent);
             return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            addItem();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        int y = 70;
        for (String s : list) {
            int color = 0xAAAAAA;
            if (mouseX >= this.width / 2 - 90 && mouseX <= this.width / 2 + 90 && mouseY >= y && mouseY < y + 10) {
                color = 0xFFFFFF; // Highlight on hover
            }
            context.drawText(this.textRenderer, s, this.width / 2 - 90, y, color, true);
            y += 10;
            if (y > this.height - 40) break;
        }
    }
}
