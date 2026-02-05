package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * GhostHand - Interact with blocks through walls by scanning blocks in view direction
 */
public class GhostHand extends Module {
    private NumberSetting reach = new NumberSetting("Reach", 4.5, 3.0, 10.0, 0.5);
    private BooleanSetting rightClickOnly = new BooleanSetting("Right Click Only", true);
    
    private int cooldown = 0;

    public GhostHand() {
        super("GhostHand", "Interact with blocks through walls.", Category.WORLD);
        addSetting(reach);
        addSetting(rightClickOnly);
    }

    @Override
    public void onTick() {
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (!mc.options.useKey.isPressed()) return;

        // Scan for blocks along view direction, ignoring obstructions
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d direction = mc.player.getRotationVec(1.0f);
        double maxReach = reach.getValue();
        
        // Step along the ray and check for interactable blocks (ignoring walls)
        for (double d = 0.5; d <= maxReach; d += 0.25) {
            Vec3d checkPos = eyePos.add(direction.multiply(d));
            BlockPos blockPos = new BlockPos((int) Math.floor(checkPos.x), (int) Math.floor(checkPos.y), (int) Math.floor(checkPos.z));
            
            // Skip air blocks
            if (mc.world.getBlockState(blockPos).isAir()) continue;
            
            // Check if this block is interactable (has block entity like chest, furnace, etc.)
            // Block entities include chests, furnaces, dispensers, hoppers, etc.
            if (mc.world.getBlockEntity(blockPos) != null) {
                
                // Create a hit result pointing at this block
                Vec3d hitVec = blockPos.toCenterPos();
                Direction side = getClosestFace(eyePos, blockPos);
                BlockHitResult hit = new BlockHitResult(hitVec, side, blockPos, false);
                
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                cooldown = 4;
                return;
            }
        }
    }
    
    /**
     * Gets the face of the block that the player is looking at.
     * The face returned is the one that would be visible from the player's position.
     */
    private Direction getClosestFace(Vec3d eyePos, BlockPos blockPos) {
        Vec3d blockCenter = blockPos.toCenterPos();
        Vec3d diff = eyePos.subtract(blockCenter);
        
        // Find the face closest to the player (the face they would see)
        double absX = Math.abs(diff.x);
        double absY = Math.abs(diff.y);
        double absZ = Math.abs(diff.z);
        
        if (absX > absY && absX > absZ) {
            // Player is east of block -> looking at EAST face; west of block -> looking at WEST face
            return diff.x > 0 ? Direction.EAST : Direction.WEST;
        } else if (absY > absZ) {
            return diff.y > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return diff.z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
