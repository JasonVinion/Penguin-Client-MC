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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;

import java.util.ArrayList;
import java.util.List;

public class ESP extends Module {
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private BooleanSetting animals = new BooleanSetting("Animals", true);
    private BooleanSetting showArmor = new BooleanSetting("Show Armor", false);
    private BooleanSetting showArmorForMobs = new BooleanSetting("Armor For Mobs", false);
    private NumberSetting range = new NumberSetting("Range", 100, 10, 200, 1);
    private NumberSetting updateInterval = new NumberSetting("Update Ticks", 5, 1, 20, 1);
    
    private int tickCounter = 0;
    private List<Entity> cachedEntities = new ArrayList<>();

    public ESP() {
        super("ESP", "Draws boxes around entities making them visible through walls. Show Armor option brightens ESP for armored entities.", Category.RENDER);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(showArmor);
        addSetting(showArmorForMobs);
        addSetting(range);
        addSetting(updateInterval);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        
        tickCounter++;
        int updateTicks = (int) updateInterval.getValue();
        
        // Only update entity cache periodically
        if (tickCounter >= updateTicks) {
            tickCounter = 0;
            cachedEntities.clear();
            double rangeVal = range.getValue();
            
            for (Entity e : mc.world.getEntities()) {
                if (e == mc.player) continue;
                if (e.distanceTo(mc.player) > rangeVal) continue;
                
                boolean shouldGlow = false;
                
                if (e instanceof PlayerEntity && players.isEnabled()) {
                    shouldGlow = true;
                }
                if (e instanceof Monster && mobs.isEnabled()) {
                    shouldGlow = true;
                }
                if (e instanceof AnimalEntity && animals.isEnabled()) {
                    shouldGlow = true;
                }

                if (shouldGlow) {
                    e.setGlowing(true);
                    cachedEntities.add(e);
                } else {
                    if (e instanceof PlayerEntity || e instanceof Monster || e instanceof AnimalEntity) {
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
            if (e instanceof PlayerEntity || e instanceof Monster || e instanceof AnimalEntity) {
                e.setGlowing(false);
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
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Use cached entities instead of iterating all entities every frame
        for (Entity e : cachedEntities) {
            if (e == null || e.isRemoved()) continue;
            
            double x = e.getX() - camPos.x;
            double y = e.getY() - camPos.y;
            double z = e.getZ() - camPos.z;
            float w = e.getWidth() / 2;
            float h = e.getHeight();
            float r = 1f, g = 1f, b = 1f, a = 1f;

            if (e instanceof PlayerEntity) { r = 0f; g = 1f; b = 0f; }
            else if (e instanceof Monster) { r = 1f; g = 0f; b = 0f; }
            else if (e instanceof AnimalEntity) { r = 0f; g = 0f; b = 1f; }
            
            // Adjust color brightness based on armor if enabled
            if (showArmor.isEnabled() && e instanceof LivingEntity living) {
                boolean isPlayerOrMob = (e instanceof PlayerEntity) || (e instanceof Monster);
                if ((e instanceof PlayerEntity) || (showArmorForMobs.isEnabled() && isPlayerOrMob)) {
                    int armorValue = 0;
                    if (living instanceof PlayerEntity player) {
                        armorValue = player.getArmor();
                    } else {
                        // Check equipment slots for mobs
                        int count = 0;
                        if (!living.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) count++;
                        if (!living.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) count++;
                        if (!living.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) count++;
                        if (!living.getEquippedStack(EquipmentSlot.FEET).isEmpty()) count++;
                        armorValue = count * 4; // Rough estimate
                    }
                    // Brighten color based on armor (up to 20 points = max armor)
                    float armorFactor = Math.min(1.0f, armorValue / 20.0f);
                    // Add white tint based on armor (makes the color brighter/whiter)
                    r = Math.min(1.0f, r + armorFactor * 0.5f);
                    g = Math.min(1.0f, g + armorFactor * 0.5f);
                    b = Math.min(1.0f, b + armorFactor * 0.5f);
                }
            }

            // Bottom
            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z - w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z + w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x + w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z + w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)y, (float)(z - w)).color(r,g,b,a).next();

            // Top
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x + w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z + w)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x - w), (float)(y + h), (float)(z - w)).color(r,g,b,a).next();

            // Verticals
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
