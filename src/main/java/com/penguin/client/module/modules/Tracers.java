package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;

import java.util.ArrayList;
import java.util.List;

public class Tracers extends Module {
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private BooleanSetting animals = new BooleanSetting("Animals", true);
    private NumberSetting range = new NumberSetting("Range", 100, 10, 200, 1);
    private NumberSetting updateInterval = new NumberSetting("Update Ticks", 5, 1, 20, 1);
    
    private int tickCounter = 0;
    private List<Entity> cachedEntities = new ArrayList<>();

    public Tracers() {
        super("Tracers", "Draws lines from your screen center to entities.", Category.RENDER);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(range);
        addSetting(updateInterval);
    }
    
    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        
        tickCounter++;
        int updateTicks = (int) updateInterval.getValue();
        
        if (tickCounter >= updateTicks) {
            tickCounter = 0;
            cachedEntities.clear();
            double rangeVal = range.getValue();
            
            for (Entity e : mc.world.getEntities()) {
                if (e == mc.player) continue;
                if (e.distanceTo(mc.player) > rangeVal) continue;

                boolean shouldRender = false;
                if (e instanceof PlayerEntity && players.isEnabled()) shouldRender = true;
                if (e instanceof Monster && mobs.isEnabled()) shouldRender = true;
                if (e instanceof AnimalEntity && animals.isEnabled()) shouldRender = true;

                if (shouldRender) {
                    cachedEntities.add(e);
                }
            }
        }
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        if (cachedEntities.isEmpty()) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Vec3d forward = mc.player.getRotationVec(mc.getTickDelta());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (Entity e : cachedEntities) {
            if (e == null || e.isRemoved()) continue;
            
            float r = 1f, g = 1f, b = 1f, a = 1f;
            if (e instanceof PlayerEntity) { r = 0f; g = 1f; b = 0f; }
            else if (e instanceof Monster) { r = 1f; g = 0f; b = 0f; }
            else if (e instanceof AnimalEntity) { r = 0f; g = 0f; b = 1f; }

            Vec3d ePos = e.getPos().subtract(camPos).add(0, e.getHeight() / 2, 0);

            buffer.vertex(matrix, (float)forward.x, (float)forward.y, (float)forward.z).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)ePos.x, (float)ePos.y, (float)ePos.z).color(r,g,b,a).next();
        }
        tessellator.draw();
    }
}
