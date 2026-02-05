package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.ui.screen.ListEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;

import java.util.ArrayList;
import java.util.List;

public class ItemESP extends Module {
    private ActionSetting editList = new ActionSetting("Edit List...", () -> {
        MinecraftClient.getInstance().setScreen(new ListEditorScreen(null, this.itemList, list -> this.itemList = list));
    });
    private NumberSetting range = new NumberSetting("Range", 100, 10, 200, 1);
    private NumberSetting updateInterval = new NumberSetting("Update Ticks", 5, 1, 20, 1);
    private List<String> itemList = new ArrayList<>();
    
    private int tickCounter = 0;
    private List<Entity> cachedItems = new ArrayList<>();

    public ItemESP() {
        super("ItemESP", "Draws boxes around dropped items so you can find them easily.", Category.RENDER);
        addSetting(editList);
        addSetting(range);
        addSetting(updateInterval);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        tickCounter++;
        int updateTicks = (int) updateInterval.getValue();
        
        // Only update cache periodically
        if (tickCounter >= updateTicks) {
            tickCounter = 0;
            cachedItems.clear();
            double rangeVal = range.getValue();
            
            for (Entity e : mc.world.getEntities()) {
                if (e instanceof ItemEntity) {
                    if (e.distanceTo(mc.player) > rangeVal) {
                        e.setGlowing(false);
                        continue;
                    }

                    ItemEntity item = (ItemEntity) e;
                    String id = Registries.ITEM.getId(item.getStack().getItem()).toString();

                    boolean match = itemList.isEmpty() || itemList.contains(id) || itemList.contains(id.replace("minecraft:", ""));

                    if (match) {
                        e.setGlowing(true);
                        cachedItems.add(e);
                    } else {
                        e.setGlowing(false);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof ItemEntity) {
                e.setGlowing(false);
            }
        }
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        if (cachedItems.isEmpty()) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Use cached items instead of iterating all entities every frame
        for (Entity e : cachedItems) {
            if (e == null || e.isRemoved()) continue;
            
            double x = e.getX() - camPos.x;
            double y = e.getY() - camPos.y;
            double z = e.getZ() - camPos.z;
            float w = e.getWidth() / 2;
            float h = e.getHeight();
            float r = 1f, g = 1f, b = 0f, a = 1f;

            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z - w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
        }
        tessellator.draw();
    }
}
