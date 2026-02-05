package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import net.minecraft.client.render.Tessellator;

import java.util.ArrayList;
import java.util.List;

public class NameTags extends Module {
    public static NumberSetting scale = new NumberSetting("Scale", 1.0, 0.5, 3.0, 0.1);
    public static BooleanSetting players = new BooleanSetting("Players", true);
    public static BooleanSetting mobs = new BooleanSetting("Mobs", false);
    public static BooleanSetting animals = new BooleanSetting("Animals", false);
    private NumberSetting range = new NumberSetting("Range", 64, 16, 128, 8);
    private NumberSetting updateInterval = new NumberSetting("Update Ticks", 5, 1, 20, 1);
    
    private int tickCounter = 0;
    private List<Entity> cachedEntities = new ArrayList<>();

    public NameTags() {
        super("NameTags", "Shows enhanced nametags with health and armor info through walls.", Category.RENDER);
        addSetting(scale);
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

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        TextRenderer textRenderer = mc.textRenderer;

        for (Entity e : cachedEntities) {
            if (e == null || e.isRemoved()) continue;

            double x = e.getX() - camPos.x;
            double y = e.getY() + e.getHeight() + 0.5 - camPos.y;
            double z = e.getZ() - camPos.z;

            matrices.push();
            matrices.translate(x, y, z);

            Quaternionf rotation = mc.gameRenderer.getCamera().getRotation();
            matrices.multiply(rotation);

            float s = (float)scale.getValue() * 0.025f;
            matrices.scale(-s, -s, s);

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            Text text = e.getDisplayName();
            float width = textRenderer.getWidth(text) / 2f;

            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

            textRenderer.draw(text, -width, 0, 0xFFFFFFFF, false, matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 0xF000F0);

            immediate.draw();

            matrices.pop();
        }
    }
}
