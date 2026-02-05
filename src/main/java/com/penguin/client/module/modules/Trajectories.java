package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;

import java.util.Optional;
import java.util.function.Predicate;

public class Trajectories extends Module {
    private BooleanSetting bows = new BooleanSetting("Bows", true);
    private BooleanSetting pearls = new BooleanSetting("Pearls", true);
    private BooleanSetting potions = new BooleanSetting("Potions", true);
    private BooleanSetting other = new BooleanSetting("Other", true);
    private BooleanSetting showLanding = new BooleanSetting("Show Landing", true);
    private BooleanSetting showEntityHit = new BooleanSetting("Show Entity Hit", true);

    public Trajectories() {
        super("Trajectories", "Draws projectile trajectory paths showing where arrows, pearls, and throwables will land or hit entities.", Category.RENDER);
        addSetting(bows);
        addSetting(pearls);
        addSetting(potions);
        addSetting(other);
        addSetting(showLanding);
        addSetting(showEntityHit);
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) stack = mc.player.getOffHandStack();

        if (isThrowable(stack)) {
            renderTrajectory(matrices, stack);
        }
    }

    private boolean isThrowable(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BowItem || item instanceof CrossbowItem) return bows.isEnabled();
        if (item instanceof EnderPearlItem) return pearls.isEnabled();
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem) return potions.isEnabled();
        if (item instanceof SnowballItem || item instanceof EggItem || item instanceof TridentItem) return other.isEnabled();
        return false;
    }

    private void renderTrajectory(MatrixStack matrices, ItemStack stack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        
        // Calculate the direction vector
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        
        double motionX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double motionY = -Math.sin(pitchRad);
        double motionZ = Math.cos(yawRad) * Math.cos(pitchRad);

        // Calculate offsets for rendering (visibility purposes only)
        double rightOffset = 0.3;
        double forwardOffset = 0.5;
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);
        
        // Rendering offset - only affects how the trajectory appears, not where it actually goes
        double renderOffsetX = rightX * rightOffset + motionX * forwardOffset;
        double renderOffsetY = -0.1;
        double renderOffsetZ = rightZ * rightOffset + motionZ * forwardOffset;

        float power = 1.5f; // Default (Snowball/Pearl)
        float gravity = 0.03f;
        float drag = 0.99f;

        Item item = stack.getItem();
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            power = 3.0f; // Max bow
            if (mc.player.isUsingItem()) {
                int useTime = mc.player.getItemUseTime();
                float f = (float) useTime / 20.0F;
                f = (f * f + f * 2.0F) / 3.0F;
                if (f > 1.0F) f = 1.0F;
                power = f * 3.0f;
            }
            gravity = 0.05f;
        } else if (item instanceof PotionItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem) {
            power = 0.5f;
            gravity = 0.05f;
        }

        Vec3d motion = new Vec3d(motionX, motionY, motionZ).normalize().multiply(power);
        
        // Actual trajectory starts from player's eye position (for collision calculation)
        Vec3d worldPos = new Vec3d(
            mc.player.getX(),
            mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
            mc.player.getZ()
        );
        
        // Render position starts with the visual offset
        Vec3d renderPos = new Vec3d(
            mc.player.getX() + renderOffsetX - camPos.x,
            mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) + renderOffsetY - camPos.y,
            mc.player.getZ() + renderOffsetZ - camPos.z
        );
        
        Vec3d landingPos = null;
        Entity hitEntity = null;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2.0f);
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Predicate to exclude the player from entity collision checks
        Predicate<Entity> entityPredicate = entity -> 
            entity != mc.player && 
            !entity.isSpectator() && 
            entity.canHit() &&
            entity instanceof LivingEntity;

        for (int i = 0; i < 200; i++) {
            Vec3d renderStart = renderPos;
            Vec3d worldStart = worldPos;
            
            // Apply physics to both positions
            renderPos = renderPos.add(motion);
            worldPos = worldPos.add(motion);
            motion = motion.multiply(drag);
            motion = motion.subtract(0, gravity, 0);

            // Check for entity collision first (if enabled)
            if (showEntityHit.isEnabled()) {
                Box searchBox = new Box(worldStart, worldPos).expand(0.5);
                for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox, entityPredicate)) {
                    Box entityBox = entity.getBoundingBox().expand(0.3);
                    Optional<Vec3d> hit = entityBox.raycast(worldStart, worldPos);
                    if (hit.isPresent()) {
                        Vec3d hitPos = hit.get().subtract(camPos);
                        buffer.vertex(matrix, (float) renderStart.x, (float) renderStart.y, (float) renderStart.z).color(1f, 0.5f, 0f, 1f).next();
                        buffer.vertex(matrix, (float) hitPos.x, (float) hitPos.y, (float) hitPos.z).color(1f, 0.5f, 0f, 1f).next();
                        hitEntity = entity;
                        landingPos = hit.get();
                        break;
                    }
                }
                if (hitEntity != null) break;
            }

            // Check for block collision
            BlockHitResult hitResult = mc.world.raycast(new RaycastContext(
                worldStart, worldPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
            ));

            if (hitResult.getType() != HitResult.Type.MISS) {
                Vec3d hitPos = hitResult.getPos().subtract(camPos);
                buffer.vertex(matrix, (float) renderStart.x, (float) renderStart.y, (float) renderStart.z).color(0f, 1f, 0f, 1f).next();
                buffer.vertex(matrix, (float) hitPos.x, (float) hitPos.y, (float) hitPos.z).color(0f, 1f, 0f, 1f).next();
                landingPos = hitResult.getPos();
                break;
            }

            // Draw trajectory line segment (always green since we break on entity hit)
            buffer.vertex(matrix, (float) renderStart.x, (float) renderStart.y, (float) renderStart.z).color(0f, 1f, 0f, 1f).next();
            buffer.vertex(matrix, (float) renderPos.x, (float) renderPos.y, (float) renderPos.z).color(0f, 1f, 0f, 1f).next();

            // Simple ground check
            if (worldPos.y < -64) break;
        }

        tessellator.draw();

        // Draw landing/hit indicator (respect settings)
        boolean shouldShowLanding = (hitEntity != null && showEntityHit.isEnabled()) || 
                                     (hitEntity == null && showLanding.isEnabled());
        if (landingPos != null && shouldShowLanding) {
            Vec3d landingRender = landingPos.subtract(camPos);
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            
            float size = 0.3f;
            float r, g, b;
            
            if (hitEntity != null) {
                // Orange/red for entity hit
                r = 1f; g = 0.3f; b = 0f;
            } else {
                // Red for block hit
                r = 1f; g = 0f; b = 0f;
            }
            
            // Draw a cross at landing position
            buffer.vertex(matrix, (float)(landingRender.x - size), (float)landingRender.y, (float)landingRender.z).color(r, g, b, 1f).next();
            buffer.vertex(matrix, (float)(landingRender.x + size), (float)landingRender.y, (float)landingRender.z).color(r, g, b, 1f).next();
            buffer.vertex(matrix, (float)landingRender.x, (float)landingRender.y, (float)(landingRender.z - size)).color(r, g, b, 1f).next();
            buffer.vertex(matrix, (float)landingRender.x, (float)landingRender.y, (float)(landingRender.z + size)).color(r, g, b, 1f).next();
            buffer.vertex(matrix, (float)landingRender.x, (float)(landingRender.y - size), (float)landingRender.z).color(r, g, b, 1f).next();
            buffer.vertex(matrix, (float)landingRender.x, (float)(landingRender.y + size), (float)landingRender.z).color(r, g, b, 1f).next();
            
            tessellator.draw();
        }

        RenderSystem.enableDepthTest();
    }
}
