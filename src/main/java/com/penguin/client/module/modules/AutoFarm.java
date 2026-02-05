package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class AutoFarm extends Module {
    private BlockPos startPos;
    private BlockPos targetPos;
    private State state = State.IDLE;
    private int scanTimer = 0;

    private enum State {
        IDLE, WALKING_TO_CROP, BREAKING, WALKING_TO_ITEM, REPLANTING, RETURNING
    }

    public AutoFarm() {
        super("AutoFarm", "Automatically harvests and replants mature crops when walking over them.", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (MinecraftClient.getInstance().player != null) {
            startPos = MinecraftClient.getInstance().player.getBlockPos();
        }
        state = State.IDLE;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        mc.player.setSprinting(false);
        scanTimer++;

        switch (state) {
            case IDLE:
                targetPos = null;
                if (scanTimer % 20 == 0) {
                    int r = 10;
                    for (int x = -r; x <= r; x++) {
                        for (int z = -r; z <= r; z++) {
                            for (int y = -1; y <= 1; y++) {
                                BlockPos p = startPos.add(x, y, z);
                                BlockState bs = mc.world.getBlockState(p);
                                if (bs.getBlock() instanceof CropBlock) {
                                    CropBlock crop = (CropBlock) bs.getBlock();
                                    if (crop.isMature(bs)) {
                                        targetPos = p;
                                        state = State.WALKING_TO_CROP;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                if (mc.player.getBlockPos().getSquaredDistance(startPos) > 2) {
                    state = State.RETURNING;
                }
                break;
            case WALKING_TO_CROP:
                if (targetPos == null) { state = State.IDLE; return; }
                moveTo(targetPos);
                if (mc.player.getBlockPos().getSquaredDistance(targetPos) < 2.5) {
                    mc.options.forwardKey.setPressed(false);
                    state = State.BREAKING;
                }
                break;
            case BREAKING:
                 if (targetPos != null) {
                     mc.interactionManager.attackBlock(targetPos, Direction.UP);
                     mc.player.swingHand(Hand.MAIN_HAND);
                     if (mc.world.getBlockState(targetPos).isAir()) {
                         state = State.WALKING_TO_ITEM;
                     }
                 } else state = State.IDLE;
                 break;
            case WALKING_TO_ITEM:
                 List<ItemEntity> items = mc.world.getEntitiesByClass(ItemEntity.class, mc.player.getBoundingBox().expand(5), e -> true);
                 if (!items.isEmpty()) {
                     ItemEntity item = items.stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc.player))).get();
                     moveTo(item.getBlockPos());
                     if (item.distanceTo(mc.player) < 1.0) state = State.REPLANTING;
                 } else {
                     state = State.REPLANTING;
                 }
                 break;
            case REPLANTING:
                 if (targetPos != null && mc.world.getBlockState(targetPos).isAir()) {
                     BlockPos farmland = targetPos.down();
                     int seedSlot = -1;
                     for(int i=0; i<9; i++) {
                         ItemStack stack = mc.player.getInventory().getStack(i);
                         if(stack.isEmpty()) continue;
                         if(stack.getItem() == Items.WHEAT_SEEDS ||
                            stack.getItem() == Items.POTATO ||
                            stack.getItem() == Items.CARROT ||
                            stack.getItem() == Items.BEETROOT_SEEDS) {
                             seedSlot = i;
                             break;
                         }
                     }

                     if (seedSlot != -1) {
                         int prev = mc.player.getInventory().selectedSlot;
                         mc.player.getInventory().selectedSlot = seedSlot;

                         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(
                             new Vec3d(farmland.getX()+0.5, farmland.getY()+1, farmland.getZ()+0.5),
                             Direction.UP, farmland, false
                         ));
                         mc.player.swingHand(Hand.MAIN_HAND);

                         mc.player.getInventory().selectedSlot = prev;
                     }
                 }
                 state = State.IDLE;
                 break;
            case RETURNING:
                 moveTo(startPos);
                 if (mc.player.getBlockPos().getSquaredDistance(startPos) < 1.5) {
                     mc.options.forwardKey.setPressed(false);
                     state = State.IDLE;
                 }
                 break;
        }
    }

    private void moveTo(BlockPos target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double dx = target.getX() + 0.5 - mc.player.getX();
        double dz = target.getZ() + 0.5 - mc.player.getZ();
        double angle = Math.atan2(dz, dx);

        float yaw = (float) Math.toDegrees(angle) - 90;
        mc.player.setYaw(yaw);
        mc.options.forwardKey.setPressed(true);
    }
}
