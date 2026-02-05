package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.DamageUtils;
import com.penguin.client.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AutoCrystal extends Module {
    private BooleanSetting place = new BooleanSetting("Place", true);
    private BooleanSetting breakCrystal = new BooleanSetting("Break", true);
    private BooleanSetting autoObsidian = new BooleanSetting("Auto Obsidian", true);
    private BooleanSetting autoHole = new BooleanSetting("Auto Hole", false);
    private BooleanSetting autoSwitch = new BooleanSetting("Auto Switch", true);
    private NumberSetting range = new NumberSetting("Range", 5.0, 1.0, 6.0, 0.1);
    private NumberSetting delay = new NumberSetting("Delay", 2.0, 0.0, 20.0, 1.0);
    private NumberSetting minHealth = new NumberSetting("Min Health", 8.0, 0.0, 20.0, 1.0);
    private NumberSetting maxSelfDamage = new NumberSetting("Max Self Dmg", 6.0, 0.0, 20.0, 1.0);
    private NumberSetting minDamage = new NumberSetting("Min Damage", 4.0, 0.0, 20.0, 1.0);
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private BooleanSetting animals = new BooleanSetting("Animals", false);

    private int timer = 0;
    private int previousSlot = -1;

    public AutoCrystal() {
        super("AutoCrystal", "Places/breaks end crystals. Auto Obsidian places blocks. Auto Hole only places in valid holes.", Category.COMBAT);
        addSetting(place);
        addSetting(breakCrystal);
        addSetting(autoObsidian);
        addSetting(autoHole);
        addSetting(autoSwitch);
        addSetting(range);
        addSetting(delay);
        addSetting(minHealth);
        addSetting(maxSelfDamage);
        addSetting(minDamage);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
    }

    @Override
    public void onTick() {
        if (timer > 0) {
            timer--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Get target first - both place and break need a target
        Entity targetEntity = getTarget();
        
        // Break logic first (usually faster)
        boolean didAction = false;
        if (breakCrystal.isEnabled() && targetEntity instanceof LivingEntity) {
            didAction = doBreak((LivingEntity) targetEntity);
        }

        if (!didAction && place.isEnabled()) {
            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() >= minHealth.getValue()) {
                if (targetEntity instanceof LivingEntity) {
                    didAction = doPlace((LivingEntity) targetEntity);
                }
            }
        }

        if (didAction) {
            timer = (int) delay.getValue();
        }
    }

    private Entity getTarget() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return null;

        List<Entity> targets = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e != mc.player)
                .filter(e -> e.distanceTo(mc.player) <= range.getValue() * 2)
                .filter(this::isValidTarget)
                .sorted(Comparator.comparingDouble(e -> e.distanceTo(mc.player)))
                .collect(Collectors.toList());

        if (targets.isEmpty()) return null;
        return targets.get(0);
    }

    private boolean isValidTarget(Entity e) {
        if (e instanceof PlayerEntity && players.isEnabled()) return true;
        if (e instanceof Monster && mobs.isEnabled()) return true;
        if (e instanceof AnimalEntity && animals.isEnabled()) return true;
        return false;
    }

    private boolean doBreak(LivingEntity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        EndCrystalEntity bestCrystal = null;
        double bestDamage = minDamage.getValue();
        double minSelf = maxSelfDamage.getValue();

        for (Entity e : mc.world.getEntities()) {
            if (e instanceof EndCrystalEntity) {
                if (mc.player.distanceTo(e) > range.getValue()) continue;

                // Calculate damage to us
                float selfDamage = DamageUtils.getExplosionDamage(e.getPos(), 6.0f, mc.player);
                if (selfDamage > minSelf) continue;

                // Calculate damage to target
                float dmg = DamageUtils.getExplosionDamage(e.getPos(), 6.0f, target);
                if (dmg >= bestDamage) {
                    bestCrystal = (EndCrystalEntity) e;
                    bestDamage = dmg;
                }
            }
        }

        if (bestCrystal != null) {
            mc.interactionManager.attackEntity(mc.player, bestCrystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }

    private boolean doPlace(LivingEntity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockPos bestPos = null;
        BlockPos bestObsidianPos = null;
        double bestDamage = minDamage.getValue();

        // Search around the target position instead of player position
        BlockPos targetPos = target.getBlockPos();
        int r = (int) Math.ceil(range.getValue());

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getValue() * range.getValue()) continue;

                    if (canPlaceCrystal(pos)) {
                        // Crystal spawns at y+1 with center offset
                        Vec3d crystalPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

                        if (crystalPos.squaredDistanceTo(target.getPos()) > 36.0) continue;

                        float dmg = DamageUtils.getExplosionDamage(crystalPos, 6.0f, target);
                        float self = DamageUtils.getExplosionDamage(crystalPos, 6.0f, mc.player);

                        if (self > maxSelfDamage.getValue()) continue;
                        if (dmg > bestDamage) {
                            bestDamage = dmg;
                            bestPos = pos;
                        }
                    } else if (autoObsidian.isEnabled() && canPlaceObsidian(pos)) {
                        // Only place obsidian if autoHole is disabled OR if the position is a valid hole
                        boolean shouldPlace = !autoHole.isEnabled() || isValidHole(pos);
                        if (!shouldPlace) continue;
                        
                        // Check if placing obsidian here would allow a crystal
                        Vec3d crystalPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);

                        if (crystalPos.squaredDistanceTo(target.getPos()) > 36.0) continue;

                        float dmg = DamageUtils.getExplosionDamage(crystalPos, 6.0f, target);
                        float self = DamageUtils.getExplosionDamage(crystalPos, 6.0f, mc.player);

                        if (self > maxSelfDamage.getValue()) continue;
                        if (dmg > bestDamage && bestPos == null) {
                            bestDamage = dmg;
                            bestObsidianPos = pos;
                        }
                    }
                }
            }
        }

        // If we found a spot for crystal, place it
        if (bestPos != null) {
            return placeCrystalAt(bestPos);
        }
        
        // If we need to place obsidian first
        if (bestObsidianPos != null) {
            return placeObsidianAt(bestObsidianPos);
        }
        
        return false;
    }
    
    private boolean placeCrystalAt(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        boolean mainHand = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL;
        boolean offHand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;

        if (!mainHand && !offHand) {
            // Try auto switch
            if (autoSwitch.isEnabled()) {
                int crystalSlot = InventoryUtils.getSlotHotbar(Items.END_CRYSTAL);
                if (crystalSlot != -1) {
                    previousSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = crystalSlot;
                    mainHand = true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        Hand hand = mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;

        BlockHitResult hit = new BlockHitResult(pos.toCenterPos(), Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, hand, hit);
        mc.player.swingHand(hand);
        
        // Switch back if we auto-switched
        if (previousSlot != -1) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
        
        return true;
    }
    
    private boolean placeObsidianAt(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        int obsidianSlot = InventoryUtils.getSlotHotbar(Items.OBSIDIAN);
        if (obsidianSlot == -1) return false;
        
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = obsidianSlot;
        
        // Find a face to place against
        Direction placeDir = getPlaceDirection(pos);
        if (placeDir == null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            return false;
        }
        
        BlockPos neighborPos = pos.offset(placeDir);
        BlockHitResult hit = new BlockHitResult(neighborPos.toCenterPos(), placeDir.getOpposite(), neighborPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        
        mc.player.getInventory().selectedSlot = prevSlot;
        return true;
    }
    
    private Direction getPlaceDirection(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
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
    
    private boolean canPlaceObsidian(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // Check if the position is air and has a neighbor to place against
        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (!mc.world.isAir(pos.up())) return false;
        
        // Check for entities in the way
        Box box = new Box(pos);
        if (!mc.world.getOtherEntities(null, box).isEmpty()) return false;
        
        // Check if there's a solid block nearby to place against
        return getPlaceDirection(pos) != null;
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world.getBlockState(pos).getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK) return false;
        if (!mc.world.isAir(pos.up())) return false;

        // Check for entities in the way
        Box box = new Box(pos.up());
        return mc.world.getOtherEntities(null, box).isEmpty();
    }
    
    /**
     * Checks if a position is a valid hole (surrounded by obsidian/bedrock on all sides).
     * Used when Auto Hole setting is enabled.
     */
    private boolean isValidHole(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // Check all horizontal directions
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos neighborPos = pos.offset(dir);
            Block block = mc.world.getBlockState(neighborPos).getBlock();
            if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
                return false;
            }
        }
        
        // Check floor
        Block floorBlock = mc.world.getBlockState(pos.down()).getBlock();
        if (floorBlock != Blocks.OBSIDIAN && floorBlock != Blocks.BEDROCK) {
            return false;
        }
        
        return true;
    }
}
