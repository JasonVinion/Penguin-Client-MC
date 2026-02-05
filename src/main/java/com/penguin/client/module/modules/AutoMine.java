package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoMine extends Module {
    private NumberSetting range = new NumberSetting("Range", 16.0, 5.0, 32.0, 1.0);
    private NumberSetting delay = new NumberSetting("Delay", 1.0, 0.0, 10.0, 1.0);
    private BooleanSetting diamond = new BooleanSetting("Diamond", true);
    private BooleanSetting gold = new BooleanSetting("Gold", true);
    private BooleanSetting iron = new BooleanSetting("Iron", true);
    private BooleanSetting debris = new BooleanSetting("Debris", true);

    private BlockPos targetPos;
    private int timer = 0;

    public AutoMine() {
        super("AutoMine", "Automatically mines ores.", Category.WORLD);
        addSetting(range);
        addSetting(delay);
        addSetting(diamond);
        addSetting(gold);
        addSetting(iron);
        addSetting(debris);
    }

    @Override
    public void onTick() {
        if (timer > 0) {
            timer--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (targetPos == null || mc.world.getBlockState(targetPos).isAir() || mc.player.squaredDistanceTo(targetPos.toCenterPos()) > range.getValue() * range.getValue()) {
            targetPos = findBlock();
        }

        if (targetPos != null) {
            double dist = mc.player.squaredDistanceTo(targetPos.toCenterPos());
            if (dist > 16.0) { // Move closer (4 blocks)
                double dx = targetPos.getX() + 0.5 - mc.player.getX();
                double dz = targetPos.getZ() + 0.5 - mc.player.getZ();

                float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                mc.player.setYaw(yaw);
                mc.options.forwardKey.setPressed(true);

                if (mc.player.horizontalCollision && mc.player.isOnGround()) {
                    mc.player.jump();
                }
            } else {
                mc.options.forwardKey.setPressed(false);

                // Look at block
                double dx = targetPos.getX() + 0.5 - mc.player.getX();
                double dy = targetPos.getY() + 0.5 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
                double dz = targetPos.getZ() + 0.5 - mc.player.getZ();
                double dist3d = Math.sqrt(dx * dx + dy * dy + dz * dz);

                float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                float pitch = (float) -(Math.atan2(dy, dist3d) * 180.0 / Math.PI);

                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);

                mc.interactionManager.updateBlockBreakingProgress(targetPos, Direction.UP);
                mc.player.swingHand(Hand.MAIN_HAND);

                timer = (int) delay.getValue();
            }
        } else {
             mc.options.forwardKey.setPressed(false);
        }
    }

    private BlockPos findBlock() {
        MinecraftClient mc = MinecraftClient.getInstance();
        BlockPos pPos = mc.player.getBlockPos();
        int r = (int) range.getValue();

        return BlockPos.findClosest(pPos, r, r, pos -> {
            Block b = mc.world.getBlockState(pos).getBlock();
            if (diamond.isEnabled() && (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
            if (gold.isEnabled() && (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE)) return true;
            if (iron.isEnabled() && (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE)) return true;
            if (debris.isEnabled() && b == Blocks.ANCIENT_DEBRIS) return true;
            return false;
        }).orElse(null);
    }

    @Override
    public void onDisable() {
        if (MinecraftClient.getInstance().options != null) {
            MinecraftClient.getInstance().options.forwardKey.setPressed(false);
        }
    }
}
