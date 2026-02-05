package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * FindStronghold - Triangulate stronghold coordinates from eye of ender throws.
 * Detects when player uses an eye of ender and records their position/direction.
 */
public class FindStronghold extends Module {
    public static FindStronghold INSTANCE;
    
    private BooleanSetting autoThrow = new BooleanSetting("Auto Throw", false);
    
    private List<ThrowData> throwsList = new ArrayList<>();
    private int throwCooldown = 0;
    private boolean wasUsingEye = false;

    public FindStronghold() {
        super("FindStronghold", "Triangulate stronghold coordinates from eye of ender throws.", Category.WORLD);
        addSetting(autoThrow);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        throwsList.clear();
        wasUsingEye = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§a[FindStronghold] Enabled. Use eyes of ender to triangulate. Need at least 2 throws from different locations."), false);
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;

        if (throwCooldown > 0) {
            throwCooldown--;
        }

        // Detect when player uses an eye of ender
        boolean holdingEye = mc.player.getMainHandStack().getItem() == Items.ENDER_EYE || 
                            mc.player.getOffHandStack().getItem() == Items.ENDER_EYE;
        boolean usingItem = mc.options.useKey.isPressed() && holdingEye;
        
        if (usingItem && !wasUsingEye && throwCooldown == 0) {
            // Player just used an eye of ender
            recordThrow(mc.player.getPos(), mc.player.getYaw());
            throwCooldown = 20; // 1 second cooldown
        }
        wasUsingEye = usingItem;

        // Auto throw functionality
        if (autoThrow.isEnabled() && throwCooldown == 0) {
            // Check if player has eye of ender
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_EYE) {
                    // Switch to slot and use
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = i;
                    
                    mc.interactionManager.interactItem(mc.player, net.minecraft.util.Hand.MAIN_HAND);
                    recordThrow(mc.player.getPos(), mc.player.getYaw());
                    
                    mc.player.getInventory().selectedSlot = prevSlot;
                    throwCooldown = 60; // Wait 3 seconds between throws
                    break;
                }
            }
        }
    }

    /**
     * Record an eye of ender throw for triangulation
     */
    public void recordThrow(Vec3d position, float yaw) {
        throwsList.add(new ThrowData(position, yaw));
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§a[FindStronghold] Recorded throw #" + throwsList.size() + " at yaw " + String.format("%.1f", yaw)), false);
        }
        
        if (throwsList.size() >= 2) {
            calculateStronghold();
        }
    }

    private void calculateStronghold() {
        if (throwsList.size() < 2) return;
        
        // Use last two throws for triangulation
        ThrowData throw1 = throwsList.get(throwsList.size() - 2);
        ThrowData throw2 = throwsList.get(throwsList.size() - 1);
        
        // Convert yaw to direction vectors
        // Note: Minecraft yaw is 0 at south, 90 at west, 180 at north, 270 at east
        double angle1 = Math.toRadians(-throw1.yaw + 90);
        double angle2 = Math.toRadians(-throw2.yaw + 90);
        
        double dx1 = Math.cos(angle1);
        double dz1 = Math.sin(angle1);
        double dx2 = Math.cos(angle2);
        double dz2 = Math.sin(angle2);
        
        // Calculate intersection point
        double x1 = throw1.position.x;
        double z1 = throw1.position.z;
        double x2 = throw2.position.x;
        double z2 = throw2.position.z;
        
        // Solve for intersection using parametric line equations
        double denominator = dx1 * dz2 - dx2 * dz1;
        if (Math.abs(denominator) < 0.001) {
            MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§c[FindStronghold] Lines are parallel! Move to a different location and try again."), false);
            return;
        }
        
        double t1 = ((x2 - x1) * dz2 - (z2 - z1) * dx2) / denominator;
        double t2 = ((x2 - x1) * dz1 - (z2 - z1) * dx1) / denominator;
        
        double strongholdX = x1 + t1 * dx1;
        double strongholdZ = z1 + t1 * dz1;
        
        // Check if the intersection point is in front of both throws (not behind)
        if (t1 < 0 || t2 < 0) {
            MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§c[FindStronghold] Intersection is behind one of your throws. Move farther and throw again."), false);
            return;
        }
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            double distance = Math.sqrt(Math.pow(strongholdX - mc.player.getX(), 2) + Math.pow(strongholdZ - mc.player.getZ(), 2));
            mc.player.sendMessage(
                Text.literal(String.format("§a[FindStronghold] Estimated coordinates: X=%.0f, Z=%.0f (%.0f blocks away)", strongholdX, strongholdZ, distance)), false);
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§c[FindStronghold] Disabled. Throws cleared."), false);
        }
        throwsList.clear();
    }

    private static class ThrowData {
        Vec3d position;
        float yaw;
        
        ThrowData(Vec3d position, float yaw) {
            this.position = position;
            this.yaw = yaw;
        }
    }
}
