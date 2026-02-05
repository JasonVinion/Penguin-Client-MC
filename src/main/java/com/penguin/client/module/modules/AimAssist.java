package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class AimAssist extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Linear", "Linear", "Curve");
    private NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 10.0, 0.1);
    private NumberSetting speed = new NumberSetting("Speed", 5.0, 1.0, 50.0, 1.0);
    private NumberSetting smoothness = new NumberSetting("Smoothness", 4.0, 1.0, 20.0, 0.5);
    private NumberSetting fov = new NumberSetting("FOV", 90.0, 10.0, 360.0, 5.0);
    private BooleanSetting showFOV = new BooleanSetting("Show FOV Circle", false);
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private BooleanSetting animals = new BooleanSetting("Animals", false);
    private BooleanSetting clickOnly = new BooleanSetting("Click Only", true);

    public AimAssist() {
        super("AimAssist", "Smoothly adjusts your aim toward nearby valid targets.", Category.COMBAT);
        addSetting(mode);
        addSetting(range);
        addSetting(speed);
        addSetting(smoothness);
        addSetting(fov);
        addSetting(showFOV);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(clickOnly);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (clickOnly.isEnabled() && !mc.options.attackKey.isPressed()) return;

        Entity target = null;
        double minDist = range.getValue();

        for (Entity e : mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().expand(range.getValue()))) {
            double dist = e.distanceTo(mc.player);
            if (dist > minDist) continue;

            boolean valid = false;
            if (e instanceof PlayerEntity && players.isEnabled()) valid = true;
            if (e instanceof Monster && mobs.isEnabled()) valid = true;
            if (e instanceof AnimalEntity && animals.isEnabled()) valid = true;

            if (valid) {
                if (!isInFOV(e)) valid = false;
            }

            if (valid) {
                target = e;
                minDist = dist;
            }
        }

        if (target != null) {
            double dx = target.getX() - mc.player.getX();
            double dy = (target.getY() + target.getEyeHeight(target.getPose()) * 0.8) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
            double dz = target.getZ() - mc.player.getZ();

            double dist = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            float pitch = (float) -(Math.atan2(dy, dist) * 180.0 / Math.PI);

            float deltaYaw = MathHelper.wrapDegrees(yaw - mc.player.getYaw());
            float deltaPitch = MathHelper.wrapDegrees(pitch - mc.player.getPitch());

            if (mode.getMode().equals("Linear")) {
                float speedVal = (float) speed.getValue();
                if (deltaYaw > speedVal) deltaYaw = speedVal;
                if (deltaYaw < -speedVal) deltaYaw = -speedVal;
                if (deltaPitch > speedVal) deltaPitch = speedVal;
                if (deltaPitch < -speedVal) deltaPitch = -speedVal;
            } else {
                // Curve/Smooth mode
                float smoothVal = (float) smoothness.getValue();
                deltaYaw /= smoothVal;
                deltaPitch /= smoothVal;
            }

            mc.player.setYaw(mc.player.getYaw() + deltaYaw);
            mc.player.setPitch(mc.player.getPitch() + deltaPitch);
        }
    }

    private boolean isInFOV(Entity e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double dx = e.getX() - mc.player.getX();
        double dz = e.getZ() - mc.player.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float delta = Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw()));
        return delta <= fov.getValue() / 2.0;
    }
    
    @Override
    public void onRender(DrawContext context) {
        if (!showFOV.isEnabled()) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;
        
        // Draw FOV circle
        double fovAngle = Math.toRadians(fov.getValue() / 2.0);
        int radius = (int) (Math.tan(fovAngle) * 100); // Scale for visibility
        radius = Math.min(radius, Math.min(centerX, centerY) - 10);
        
        // Draw circle outline using line segments
        int segments = 32;
        int color = 0x80FFFFFF; // Semi-transparent white
        
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;
            
            int x1 = centerX + (int)(Math.cos(angle1) * radius);
            int y1 = centerY + (int)(Math.sin(angle1) * radius);
            int x2 = centerX + (int)(Math.cos(angle2) * radius);
            int y2 = centerY + (int)(Math.sin(angle2) * radius);
            
            // Draw line segment using horizontal/vertical approximation
            if (Math.abs(x2 - x1) >= Math.abs(y2 - y1)) {
                context.drawHorizontalLine(Math.min(x1, x2), Math.max(x1, x2), (y1 + y2) / 2, color);
            } else {
                context.drawVerticalLine((x1 + x2) / 2, Math.min(y1, y2), Math.max(y1, y2), color);
            }
        }
    }
}
