package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BowAim extends Module {
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private BooleanSetting animals = new BooleanSetting("Animals", false);
    private NumberSetting range = new NumberSetting("Range", 20.0, 5.0, 50.0, 1.0);
    private NumberSetting smoothness = new NumberSetting("Smoothness", 5.0, 1.0, 20.0, 0.5);
    private NumberSetting fov = new NumberSetting("FOV", 90.0, 10.0, 180.0, 5.0);
    private BooleanSetting showFOV = new BooleanSetting("Show FOV Circle", true);
    private BooleanSetting prediction = new BooleanSetting("Predict Movement", true);

    public BowAim() {
        super("BowAim", "Automatically aims bow at entities. Configure range, smoothness, and FOV to customize behavior.", Category.COMBAT);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(range);
        addSetting(smoothness);
        addSetting(fov);
        addSetting(showFOV);
        addSetting(prediction);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isUsingItem() && mc.player.getActiveItem().getItem() == Items.BOW) {
             List<Entity> targets = java.util.stream.StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                 .filter(e -> e != mc.player)
                 .filter(e -> e instanceof LivingEntity)
                 .filter(e -> !(e instanceof ArmorStandEntity))
                 .filter(e -> mc.player.distanceTo(e) <= range.getValue())
                 .filter(e -> mc.player.canSee(e))
                 .filter(e -> isInFOV(e))
                 .filter(e -> {
                     if (e instanceof PlayerEntity) return players.isEnabled();
                     if (e instanceof Monster) return mobs.isEnabled();
                     if (e instanceof AnimalEntity) return animals.isEnabled();
                     return false;
                 })
                 .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
                 .collect(Collectors.toList());

             if (!targets.isEmpty()) {
                 Entity target = targets.get(0);
                 aimAt(target);
             }
        }
    }
    
    private boolean isInFOV(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        double posX = target.getX() - mc.player.getX();
        double posZ = target.getZ() - mc.player.getZ();
        
        float targetYaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float playerYaw = MathHelper.wrapDegrees(mc.player.getYaw());
        float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - playerYaw));
        
        return yawDiff <= fov.getValue() / 2.0;
    }
    
    private void aimAt(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        double posX = target.getX() - mc.player.getX();
        double posY = (target.getY() + target.getEyeHeight(target.getPose())) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double posZ = target.getZ() - mc.player.getZ();
        
        // Get bow charge and calculate arrow speed
        int useTime = mc.player.getItemUseTime();
        float chargePercent = Math.min(useTime / 20.0f, 1.0f);
        // Arrow velocity in blocks/tick. Full charge = 3.0 blocks/tick
        float arrowSpeed = chargePercent * 3.0f;
        
        // Predict movement if enabled
        if (prediction.isEnabled() && target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) target;
            double distance = mc.player.distanceTo(target);
            
            if (arrowSpeed > 0.1f) {
                double travelTime = distance / (arrowSpeed * 20.0); // Rough estimate
                
                // Add velocity prediction
                posX += living.getVelocity().x * travelTime * 20;
                posZ += living.getVelocity().z * travelTime * 20;
            }
        }
        
        double dist = Math.sqrt(posX * posX + posZ * posZ);

        float targetYaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        
        // Calculate pitch with arrow drop compensation
        float targetPitch = calculatePitchWithArrowDrop(dist, posY, arrowSpeed);
        
        // Apply smoothness
        float smoothFactor = (float) (21.0 - smoothness.getValue()) / 20.0f; // Higher smoothness = slower movement
        
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        
        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;
        
        float newYaw = currentYaw + yawDiff * smoothFactor;
        float newPitch = currentPitch + pitchDiff * smoothFactor;
        
        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90, 90));
    }
    
    /**
     * Calculate the required pitch angle accounting for arrow drop (gravity).
     * Uses projectile motion physics to determine the correct launch angle.
     * 
     * @param horizontalDist Horizontal distance to target
     * @param verticalDist Vertical distance to target (positive = above player)
     * @param arrowSpeed Arrow initial velocity in blocks/tick
     * @return The pitch angle in degrees
     */
    private float calculatePitchWithArrowDrop(double horizontalDist, double verticalDist, float arrowSpeed) {
        // Convert to blocks/second (20 ticks per second)
        double v = arrowSpeed * 20.0;
        
        // Minecraft arrow gravity is 0.05 blocks/tick^2 = 20 blocks/s^2
        double g = 20.0;
        
        // If arrow speed is too low, just aim directly
        if (v < 1.0) {
            return (float) -(Math.atan2(verticalDist, horizontalDist) * 180.0 / Math.PI);
        }
        
        double x = horizontalDist;
        double y = verticalDist;
        
        // Solve projectile motion equation for launch angle
        // y = x*tan(θ) - (g*x²)/(2*v²*cos²(θ))
        // Using the quadratic formula solution:
        double vSquared = v * v;
        double discriminant = vSquared * vSquared - g * (g * x * x + 2 * y * vSquared);
        
        if (discriminant < 0) {
            // Target is out of range, aim as high as possible towards target
            return (float) -(Math.atan2(verticalDist, horizontalDist) * 180.0 / Math.PI) - 10.0f;
        }
        
        // Two solutions exist, use the lower angle (flatter trajectory)
        double sqrtDisc = Math.sqrt(discriminant);
        double angle1 = Math.atan((vSquared - sqrtDisc) / (g * x));
        
        // Convert to Minecraft pitch (negative = looking up)
        return (float) -(angle1 * 180.0 / Math.PI);
    }

    @Override
    public void onRender(DrawContext context) {
        if (!showFOV.isEnabled()) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!mc.player.isUsingItem() || mc.player.getActiveItem().getItem() != Items.BOW) return;
        
        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;
        
        // Draw FOV circle
        double fovAngle = Math.toRadians(fov.getValue() / 2.0);
        int radius = (int) (Math.tan(fovAngle) * 100); // Scale for visibility
        radius = Math.min(radius, Math.min(centerX, centerY) - 10);
        
        // Draw circle using line segments (efficient approach with 16 segments)
        int segments = 16;
        int prevX = centerX + radius;
        int prevY = centerY;
        
        for (int i = 1; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            int x = centerX + (int)(Math.cos(angle) * radius);
            int y = centerY + (int)(Math.sin(angle) * radius);
            
            // Draw horizontal line for this segment
            context.drawHorizontalLine(Math.min(prevX, x), Math.max(prevX, x), (prevY + y) / 2, 0x8055FF55);
            
            prevX = x;
            prevY = y;
        }
    }
}
