package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AirPlace - Place blocks in midair
 */
public class AirPlace extends Module {
    private NumberSetting range = new NumberSetting("Range", 5.0, 3.0, 10.0, 0.5);
    
    private int cooldown = 0;

    public AirPlace() {
        super("AirPlace", "Place blocks in midair without needing a surface.", Category.WORLD);
        addSetting(range);
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
        
        // Check if holding a block item
        if (!(mc.player.getMainHandStack().getItem() instanceof net.minecraft.item.BlockItem) &&
            !(mc.player.getOffHandStack().getItem() instanceof net.minecraft.item.BlockItem)) {
            return;
        }

        // Create a virtual hit result in the air
        Vec3d pos = mc.player.getCameraPosVec(1.0f);
        Vec3d direction = mc.player.getRotationVec(1.0f);
        Vec3d target = pos.add(direction.multiply(range.getValue()));
        
        BlockPos blockPos = new BlockPos((int) Math.floor(target.x), (int) Math.floor(target.y), (int) Math.floor(target.z));
        
        // Only place if the position is air
        if (!mc.world.getBlockState(blockPos).isAir()) return;
        
        Hand hand = mc.player.getMainHandStack().getItem() instanceof net.minecraft.item.BlockItem ? 
                    Hand.MAIN_HAND : Hand.OFF_HAND;
        
        BlockHitResult hit = new BlockHitResult(target, Direction.DOWN, blockPos, false);
        mc.interactionManager.interactBlock(mc.player, hand, hit);
        mc.player.swingHand(hand);
        
        cooldown = 4;
    }
}
