package com.penguin.client.ui.screen;

import com.penguin.client.module.modules.Replenish;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ReplenishConfigScreen extends Screen {
    private final Screen parent;
    private final Replenish module;
    private final Map<Integer, Item> layout = new HashMap<>();
    private Item selectedItem = null;

    // UI Constants
    private static final int SLOT_SIZE = 18;
    private int guiLeft;
    private int guiTop;

    public ReplenishConfigScreen(Screen parent, Replenish module) {
        super(Text.of("Replenish Layout"));
        this.parent = parent;
        this.module = module;
        loadLayout();
    }

    private void loadLayout() {
        String config = module.getLayoutConfig().getValue();
        if (config == null || config.isEmpty()) return;

        String[] parts = config.split(",");
        for (String part : parts) {
            String[] pair = part.split(":");
            if (pair.length == 2) {
                try {
                    int slot = Integer.parseInt(pair[0]);
                    Identifier id = Identifier.tryParse(pair[1]);
                    if (id != null) {
                        Item item = Registries.ITEM.get(id);
                        if (item != null) {
                            layout.put(slot, item);
                        }
                    }
                } catch (Exception e) {
                    // Ignore malformed
                }
            }
        }
    }

    private void saveLayout() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Item> entry : layout.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(entry.getKey()).append(":").append(Registries.ITEM.getId(entry.getValue()).toString());
        }
        module.getLayoutConfig().setValue(sb.toString());
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (this.width - 162) / 2;
        guiTop = (this.height - 180) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        // Draw Config Slots Title
        context.drawText(this.textRenderer, "Target Hotbar Layout", guiLeft, guiTop - 15, 0xFFFFFF, true);

        // Draw Config Slots (0-8)
        for (int i = 0; i < 9; i++) {
            int x = guiLeft + i * SLOT_SIZE;
            int y = guiTop;
            drawSlot(context, x, y, layout.get(i));

            if (isHovered(mouseX, mouseY, x, y)) {
                context.drawBorder(x, y, SLOT_SIZE, SLOT_SIZE, 0xFFFFFFFF);
            }
        }

        // Draw Inventory Title
        context.drawText(this.textRenderer, "Inventory (Click to select)", guiLeft, guiTop + 30, 0xFFAAAAAA, true);

        if (selectedItem != null) {
             context.drawText(this.textRenderer, "Selected: " + selectedItem.getName().getString(), guiLeft, guiTop + 150, 0xFF55FF55, true);
        }

        if (this.client != null && this.client.player != null) {
            for (int i = 0; i < 36; i++) {
                 ItemStack stack = this.client.player.getInventory().getStack(i);
                 int x, y;
                 if (i < 9) {
                     // Hotbar
                     x = guiLeft + i * SLOT_SIZE;
                     y = guiTop + 45 + 3 * SLOT_SIZE + 4;
                 } else {
                     // Main
                     int row = (i - 9) / 9;
                     int col = (i - 9) % 9;
                     x = guiLeft + col * SLOT_SIZE;
                     y = guiTop + 45 + row * SLOT_SIZE;
                 }

                 drawSlot(context, x, y, stack);
                 if (isHovered(mouseX, mouseY, x, y)) {
                     context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x55FFFFFF);
                 }
            }
        }
    }

    private void drawSlot(DrawContext context, int x, int y, Item item) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x88000000);
        context.drawBorder(x, y, SLOT_SIZE, SLOT_SIZE, 0xFF555555);
        if (item != null) {
            context.drawItem(item.getDefaultStack(), x + 1, y + 1);
        }
    }

    private void drawSlot(DrawContext context, int x, int y, ItemStack stack) {
        context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x88000000);
        context.drawBorder(x, y, SLOT_SIZE, SLOT_SIZE, 0xFF555555);
        if (!stack.isEmpty()) {
            context.drawItem(stack, x + 1, y + 1);
            context.drawItemInSlot(this.textRenderer, stack, x + 1, y + 1);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check Config Slots
        for (int i = 0; i < 9; i++) {
            int x = guiLeft + i * SLOT_SIZE;
            int y = guiTop;
            if (isHovered(mouseX, mouseY, x, y)) {
                if (button == 1) { // Right click to clear
                    layout.remove(i);
                    saveLayout();
                    return true;
                }
                if (button == 0 && selectedItem != null) {
                    layout.put(i, selectedItem);
                    saveLayout();
                    return true;
                }
            }
        }

        // Check Inventory Slots
        if (this.client != null && this.client.player != null) {
            for (int i = 0; i < 36; i++) {
                 int x, y;
                 if (i < 9) {
                     x = guiLeft + i * SLOT_SIZE;
                     y = guiTop + 45 + 3 * SLOT_SIZE + 4;
                 } else {
                     int row = (i - 9) / 9;
                     int col = (i - 9) % 9;
                     x = guiLeft + col * SLOT_SIZE;
                     y = guiTop + 45 + row * SLOT_SIZE;
                 }

                 if (isHovered(mouseX, mouseY, x, y)) {
                     ItemStack stack = this.client.player.getInventory().getStack(i);
                     if (!stack.isEmpty()) {
                         this.selectedItem = stack.getItem();
                         return true;
                     }
                 }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
