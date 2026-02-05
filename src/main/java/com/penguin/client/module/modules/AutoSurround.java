package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * AutoSurround - Automatically surrounds player's feet with obsidian for protection
 */
public class AutoSurround extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Normal", "Normal", "Full", "Feet Only");
    private BooleanSetting autoDisable = new BooleanSetting("Auto Disable", true);
    private BooleanSetting center = new BooleanSetting("Center", true);
    private BooleanSetting rotate = new BooleanSetting("Rotate", false);
    private NumberSetting delay = new NumberSetting("Delay", 1.0, 0.0, 10.0, 1.0);
    private NumberSetting blocksPerTick = new NumberSetting("Blocks/Tick", 4.0, 1.0, 8.0, 1.0);
    
    private int tickDelay = 0;
    private boolean didCenter = false;
    private List<BlockPos> placementQueue = new ArrayList<>();

    public AutoSurround() {
        super("AutoSurround", "Automatically surrounds your feet with obsidian for protection.", Category.COMBAT);
        addSetting(mode);
        addSetting(autoDisable);
        addSetting(center);
        addSetting(rotate);
        addSetting(delay);
        addSetting(blocksPerTick);
    }

    @Override
    public void onEnable() {
        didCenter = false;
        placementQueue.clear();
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        // Center player on block if enabled
        if (center.isEnabled() && !didCenter) {
            BlockPos playerPos = mc.player.getBlockPos();
            double targetX = playerPos.getX() + 0.5;
            double targetZ = playerPos.getZ() + 0.5;
            mc.player.setPosition(targetX, mc.player.getY(), targetZ);
            didCenter = true;
        }
        
        // Build placement queue
        buildPlacementQueue();
    }
    
    @Override
    public void onDisable() {
        placementQueue.clear();
    }

    @Override
    public void onTick() {
        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // If queue is empty and auto-disable is on, disable
        if (placementQueue.isEmpty()) {
            if (autoDisable.isEnabled()) {
                toggle();
            } else {
                // Rebuild queue in case blocks were broken
                buildPlacementQueue();
            }
            return;
        }

        // Place blocks
        int blocksToPlace = (int) blocksPerTick.getValue();
        int placed = 0;
        
        for (int i = 0; i < blocksToPlace && !placementQueue.isEmpty(); i++) {
            if (placeBlock(mc)) {
                placed++;
            } else {
                break;
            }
        }
        
        if (placed > 0) {
            tickDelay = (int) delay.getValue();
        }
    }

    private void buildPlacementQueue() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        
        placementQueue.clear();
        BlockPos playerPos = mc.player.getBlockPos();
        String currentMode = mode.getMode();
        
        if (currentMode.equals("Feet Only")) {
            // Just the 4 cardinal directions at feet level
            addIfValid(playerPos.north());
            addIfValid(playerPos.south());
            addIfValid(playerPos.east());
            addIfValid(playerPos.west());
        } else if (currentMode.equals("Normal")) {
            // 8 blocks around feet (cardinals + corners)
            addIfValid(playerPos.north());
            addIfValid(playerPos.south());
            addIfValid(playerPos.east());
            addIfValid(playerPos.west());
            addIfValid(playerPos.north().east());
            addIfValid(playerPos.north().west());
            addIfValid(playerPos.south().east());
            addIfValid(playerPos.south().west());
        } else if (currentMode.equals("Full")) {
            // Normal + layer above
            addIfValid(playerPos.north());
            addIfValid(playerPos.south());
            addIfValid(playerPos.east());
            addIfValid(playerPos.west());
            addIfValid(playerPos.north().east());
            addIfValid(playerPos.north().west());
            addIfValid(playerPos.south().east());
            addIfValid(playerPos.south().west());
            
            // Second layer
            BlockPos above = playerPos.up();
            addIfValid(above.north());
            addIfValid(above.south());
            addIfValid(above.east());
            addIfValid(above.west());
            addIfValid(above.north().east());
            addIfValid(above.north().west());
            addIfValid(above.south().east());
            addIfValid(above.south().west());
        }
    }

    private void addIfValid(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!mc.world.getBlockState(pos).isAir() && !mc.world.getBlockState(pos).isReplaceable()) {
            return;
        }
        
        // Check no entities in the way (except player)
        Box box = new Box(pos);
        if (mc.world.getOtherEntities(mc.player, box, e -> e instanceof LivingEntity).isEmpty()) {
            placementQueue.add(pos);
        }
    }

    private boolean placeBlock(MinecraftClient mc) {
        // Check for obsidian
        int obsidianSlot = InventoryUtils.getSlotHotbar(Items.OBSIDIAN);
        if (obsidianSlot == -1) {
            placementQueue.clear();
            if (autoDisable.isEnabled()) {
                toggle();
            }
            return false;
        }
        
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = obsidianSlot;
        
        // Try to place next block
        int attempts = 0;
        while (!placementQueue.isEmpty() && attempts < 5) {
            attempts++;
            BlockPos pos = placementQueue.remove(0);
            
            // Validate position
            if (!mc.world.getBlockState(pos).isAir() && !mc.world.getBlockState(pos).isReplaceable()) {
                continue;
            }

            // Check if entity is blocking
            if (!mc.world.getOtherEntities(null, new Box(pos)).isEmpty()) {
                continue;
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
            
            mc.player.getInventory().selectedSlot = prevSlot;
            return true;
        }
        
        mc.player.getInventory().selectedSlot = prevSlot;
        return false;
    }

    private Direction getPlaceDirection(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            Block block = mc.world.getBlockState(neighbor).getBlock();
            if (block != Blocks.AIR && block != Blocks.WATER && block != Blocks.LAVA && !mc.world.getBlockState(neighbor).isReplaceable()) {
                return dir;
            }
        }
        return null;
    }
}
