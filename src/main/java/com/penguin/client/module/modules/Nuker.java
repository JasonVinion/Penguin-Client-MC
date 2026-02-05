package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;

public class Nuker extends Module {
    private NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.1);
    private ModeSetting mode = new ModeSetting("Mode", "All", "All", "Creative");
    private NumberSetting delay = new NumberSetting("Delay", 1.0, 0.0, 20.0, 1.0);
    private NumberSetting blocksPerTick = new NumberSetting("Blocks Per Tick", 1.0, 1.0, 10.0, 1.0);
    
    private int timer = 0;

    public Nuker() {
        super("Nuker", "Breaks all blocks around you. Note: In 'All' mode, blocks per tick is limited to 1 due to server-side restrictions.", Category.WORLD);
        addSetting(range);
        addSetting(mode);
        addSetting(delay);
        addSetting(blocksPerTick);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (timer > 0) {
            timer--;
            return;
        }

        BlockPos playerPos = mc.player.getBlockPos();
        int searchRadius = (int) range.getValue();
        int blocksBroken = 0;
        int maxBlocks = (int) blocksPerTick.getValue();
        
        // In "All" mode, limit to 1 block per tick due to server-side restrictions
        if (mode.getMode().equals("All")) {
            maxBlocks = 1;
        }
        
        boolean isCreative = mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE;
        boolean useCreativeBreaking = mode.getMode().equals("Creative") || isCreative;

        for (int y = searchRadius; y >= -searchRadius; y--) {
            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    if (blocksBroken >= maxBlocks) {
                        timer = (int) delay.getValue();
                        return;
                    }
                    
                    BlockPos pos = playerPos.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    
                    if (state.isAir()) continue;
                    if (state.getHardness(mc.world, pos) < 0) continue; // Skip unbreakable blocks
                    
                    if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getValue() * range.getValue()) continue;

                    if (useCreativeBreaking) {
                        // Creative mode - send instant break packets
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.UP));
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                        mc.player.swingHand(Hand.MAIN_HAND);
                        blocksBroken++;
                    } else {
                        // Survival mode - use the interaction manager to properly break blocks
                        if (mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP)) {
                            mc.player.swingHand(Hand.MAIN_HAND);
                        }
                        blocksBroken++;
                    }
                }
            }
        }
        
        if (blocksBroken > 0) {
            timer = (int) delay.getValue();
        }
    }
}
