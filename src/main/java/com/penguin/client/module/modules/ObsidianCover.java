package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.InventoryUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Automatically covers a target entity in obsidian, fully surrounding them.
 * Modes: Fast (places as fast as possible), Legit (slower, more human-like), Instant (tries all at once)
 */
public class ObsidianCover extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Fast", "Fast", "Legit", "Instant");
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting villagers = new BooleanSetting("Villagers", false);
    private BooleanSetting mobs = new BooleanSetting("Hostile Mobs", false);
    private BooleanSetting passives = new BooleanSetting("Passive Mobs", false);
    private NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.5);
    private NumberSetting delay = new NumberSetting("Delay", 1.0, 0.0, 10.0, 1.0);
    private BooleanSetting autoSwitch = new BooleanSetting("Auto Switch", true);
    private BooleanSetting rotate = new BooleanSetting("Rotate", false);
    private NumberSetting blocksPerTick = new NumberSetting("Blocks/Tick", 2.0, 1.0, 8.0, 1.0);
    
    private int timer = 0;
    private Entity currentTarget = null;
    private List<BlockPos> placementQueue = new ArrayList<>();
    private int previousSlot = -1;

    public ObsidianCover() {
        super("ObsidianCover", "Surrounds targets in obsidian. Modes: Fast/Legit/Instant. Ensures complete coverage.", Category.COMBAT);
        addSetting(mode);
        addSetting(players);
        addSetting(villagers);
        addSetting(mobs);
        addSetting(passives);
        addSetting(range);
        addSetting(delay);
        addSetting(autoSwitch);
        addSetting(rotate);
        addSetting(blocksPerTick);
    }

    @Override
    public void onEnable() {
        currentTarget = null;
        placementQueue.clear();
        previousSlot = -1;
    }
    
    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
    }

    @Override
    public void onTick() {
        if (timer > 0) {
            timer--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        String currentMode = mode.getMode();
        
        // If we have an active placement queue, continue placing
        if (!placementQueue.isEmpty() && currentTarget != null) {
            int blocksToPlace = currentMode.equals("Instant") ? placementQueue.size() : 
                               (int) blocksPerTick.getValue();
            
            int placed = 0;
            for (int i = 0; i < blocksToPlace && !placementQueue.isEmpty(); i++) {
                if (placeNextBlock(mc)) {
                    placed++;
                } else {
                    break; // Stop if placement fails
                }
            }
            
            if (placed > 0) {
                int delayAmount = currentMode.equals("Fast") ? (int) delay.getValue() :
                                 currentMode.equals("Legit") ? (int) delay.getValue() * 2 :
                                 0; // Instant has no delay
                timer = delayAmount;
            }
            return;
        }

        // Find a new target
        Entity target = findTarget(mc);
        if (target == null) return;
        
        currentTarget = target;
        placementQueue = buildPlacementQueue(target.getBlockPos());
        
        // Try to place first block(s) immediately
        if (!placementQueue.isEmpty()) {
            int blocksToPlace = currentMode.equals("Instant") ? placementQueue.size() : 1;
            int placed = 0;
            
            for (int i = 0; i < blocksToPlace && !placementQueue.isEmpty(); i++) {
                if (placeNextBlock(mc)) {
                    placed++;
                } else {
                    break;
                }
            }
            
            if (placed > 0) {
                int delayAmount = currentMode.equals("Fast") ? (int) delay.getValue() :
                                 currentMode.equals("Legit") ? (int) delay.getValue() * 2 :
                                 0;
                timer = delayAmount;
            }
        }
    }

    private Entity findTarget(MinecraftClient mc) {
        double rangeVal = range.getValue();
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;
        
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!isValidTarget(e)) continue;
            
            double dist = mc.player.squaredDistanceTo(e);
            if (dist > rangeVal * rangeVal) continue;
            
            if (dist < closestDist) {
                closest = e;
                closestDist = dist;
            }
        }
        
        return closest;
    }

    private boolean isValidTarget(Entity e) {
        if (e instanceof PlayerEntity && players.isEnabled()) return true;
        if (e instanceof VillagerEntity && villagers.isEnabled()) return true;
        if (e instanceof Monster && mobs.isEnabled()) return true;
        if (e instanceof AnimalEntity && passives.isEnabled()) return true;
        return false;
    }

    /**
     * Builds the queue of block positions to place obsidian.
     * Places walls and ceiling first, floor block is placed last.
     */
    private List<BlockPos> buildPlacementQueue(BlockPos targetPos) {
        List<BlockPos> queue = new ArrayList<>();
        
        // Get the target's bounding box positions (for standing entity)
        // Walls around the target at ground level and one block up (not including ceiling row)
        for (int dy = 0; dy <= 1; dy++) {
            // North wall
            addIfNeeded(queue, targetPos.add(0, dy, -1));
            // South wall
            addIfNeeded(queue, targetPos.add(0, dy, 1));
            // East wall
            addIfNeeded(queue, targetPos.add(1, dy, 0));
            // West wall  
            addIfNeeded(queue, targetPos.add(-1, dy, 0));
            // Corners
            addIfNeeded(queue, targetPos.add(1, dy, 1));
            addIfNeeded(queue, targetPos.add(1, dy, -1));
            addIfNeeded(queue, targetPos.add(-1, dy, 1));
            addIfNeeded(queue, targetPos.add(-1, dy, -1));
        }
        
        // Top row walls (at y+2)
        addIfNeeded(queue, targetPos.add(0, 2, -1));
        addIfNeeded(queue, targetPos.add(0, 2, 1));
        addIfNeeded(queue, targetPos.add(1, 2, 0));
        addIfNeeded(queue, targetPos.add(-1, 2, 0));
        addIfNeeded(queue, targetPos.add(1, 2, 1));
        addIfNeeded(queue, targetPos.add(1, 2, -1));
        addIfNeeded(queue, targetPos.add(-1, 2, 1));
        addIfNeeded(queue, targetPos.add(-1, 2, -1));
        
        // Ceiling (center, top)
        addIfNeeded(queue, targetPos.add(0, 2, 0));
        
        // Floor block is placed LAST
        addIfNeeded(queue, targetPos.add(0, -1, 0));
        
        return queue;
    }
    
    private void addIfNeeded(List<BlockPos> queue, BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // Only add if the block is air or replaceable
        if (mc.world.getBlockState(pos).isAir() || mc.world.getBlockState(pos).isReplaceable()) {
            // Check no entities in the way
            Box box = new Box(pos);
            if (mc.world.getOtherEntities(null, box, e -> e instanceof LivingEntity && e != currentTarget).isEmpty()) {
                queue.add(pos);
            }
        }
    }

    private boolean placeNextBlock(MinecraftClient mc) {
        // Use a loop to skip invalid positions
        int attempts = 0;
        while (!placementQueue.isEmpty() && attempts < 10) {
            attempts++;
            
            // Check if we have obsidian
            boolean hasObsidian = mc.player.getMainHandStack().getItem() == Items.OBSIDIAN ||
                                  mc.player.getOffHandStack().getItem() == Items.OBSIDIAN;
            
            if (!hasObsidian && autoSwitch.isEnabled()) {
                int obsidianSlot = InventoryUtils.getSlotHotbar(Items.OBSIDIAN);
                if (obsidianSlot == -1) {
                    // No obsidian available - clear queue and abort
                    placementQueue.clear();
                    currentTarget = null;
                    return false;
                }
                previousSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = obsidianSlot;
            } else if (!hasObsidian) {
                placementQueue.clear();
                currentTarget = null;
                return false;
            }
            
            BlockPos pos = placementQueue.remove(0);
            
            // Validate position is still valid
            if (!mc.world.getBlockState(pos).isAir() && !mc.world.getBlockState(pos).isReplaceable()) {
                continue; // Skip this position
            }
            
            // Check range
            if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getValue() * range.getValue()) {
                continue; // Skip out of range
            }
            
            // Rotate if enabled
            if (rotate.isEnabled()) {
                Vec3d targetVec = pos.toCenterPos();
                Vec3d playerPos = mc.player.getEyePos();
                Vec3d diff = targetVec.subtract(playerPos);
                
                double distXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
                float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
                float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distXZ));
                
                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);
            }
            
            // Find placement direction
            Direction placeDir = getPlaceDirection(pos);
            if (placeDir == null) {
                // Try to place against air as a fallback for certain positions
                continue;
            }
            
            BlockPos neighborPos = pos.offset(placeDir);
            BlockHitResult hit = new BlockHitResult(
                neighborPos.toCenterPos().add(Vec3d.of(placeDir.getOpposite().getVector()).multiply(0.5)),
                placeDir.getOpposite(), 
                neighborPos, 
                false
            );
            
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            
            // If queue is now empty, cleanup
            if (placementQueue.isEmpty()) {
                if (previousSlot != -1) {
                    mc.player.getInventory().selectedSlot = previousSlot;
                    previousSlot = -1;
                }
                currentTarget = null;
            }
            
            return true; // Successfully placed
        }
        
        // Failed to place after multiple attempts - clear and abort
        if (previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
        placementQueue.clear();
        currentTarget = null;
        return false;
    }
    
    private Direction getPlaceDirection(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (!mc.world.getBlockState(neighbor).isAir() && 
                mc.world.getBlockState(neighbor).getBlock() != Blocks.WATER &&
                mc.world.getBlockState(neighbor).getBlock() != Blocks.LAVA &&
                !mc.world.getBlockState(neighbor).isReplaceable()) {
                return dir;
            }
        }
        return null;
    }
}
