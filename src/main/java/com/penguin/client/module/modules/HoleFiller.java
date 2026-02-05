package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * HoleFiller - Automatically fills holes with blocks (obsidian or other)
 */
public class HoleFiller extends Module {
    private BooleanSetting obsidianOnly = new BooleanSetting("Obsidian Only", true);
    private BooleanSetting rotate = new BooleanSetting("Rotate", false);
    private NumberSetting range = new NumberSetting("Range", 4.0, 2.0, 6.0, 0.5);
    private NumberSetting delay = new NumberSetting("Delay", 2.0, 0.0, 10.0, 1.0);
    
    private int tickDelay = 0;

    public HoleFiller() {
        super("HoleFiller", "Automatically fills holes around you with obsidian or blocks.", Category.WORLD);
        addSetting(obsidianOnly);
        addSetting(rotate);
        addSetting(range);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Find block to use
        int blockSlot = -1;
        if (obsidianOnly.isEnabled()) {
            blockSlot = InventoryUtils.getSlotHotbar(Items.OBSIDIAN);
        } else {
            // Find any block item
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() instanceof net.minecraft.item.BlockItem) {
                    blockSlot = i;
                    break;
                }
            }
        }
        
        if (blockSlot == -1) return;

        // Find a hole to fill
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.getValue());
        
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getValue() * range.getValue()) {
                        continue;
                    }
                    
                    if (isHole(pos)) {
                        if (fillHole(pos, blockSlot)) {
                            tickDelay = (int) delay.getValue();
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isHole(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // Position must be air
        if (!mc.world.getBlockState(pos).isAir()) return false;
        
        // Must have solid block below
        if (mc.world.getBlockState(pos.down()).isAir()) return false;
        
        // Check if surrounded on all sides (forms a hole)
        int solidSides = 0;
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            Block block = mc.world.getBlockState(pos.offset(dir)).getBlock();
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || !mc.world.getBlockState(pos.offset(dir)).isAir()) {
                solidSides++;
            }
        }
        
        // Require strict hole (4 sides)
        if (solidSides != 4) return false;

        // Check for entities
        if (!mc.world.getOtherEntities(null, new Box(pos)).isEmpty()) return false;

        return true;
    }

    private boolean fillHole(BlockPos pos, int blockSlot) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;
        
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
        
        // Find placement direction (prefer placing from below)
        Direction placeDir = getPlaceDirection(pos);
        if (placeDir == null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            return false;
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

    private Direction getPlaceDirection(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // Prefer placing from below
        if (!mc.world.getBlockState(pos.down()).isAir()) {
            return Direction.DOWN;
        }
        
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (!mc.world.getBlockState(neighbor).isAir() && 
                mc.world.getBlockState(neighbor).getBlock() != Blocks.WATER &&
                mc.world.getBlockState(neighbor).getBlock() != Blocks.LAVA) {
                return dir;
            }
        }
        return null;
    }
}
