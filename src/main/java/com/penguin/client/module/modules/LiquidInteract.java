package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Completely reworked LiquidInteract: Allows placing blocks on liquids and interacting through them.
 */
public class LiquidInteract extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Place", "Place", "Both", "Click");
    private BooleanSetting requireBlock = new BooleanSetting("Require Block Item", true);
    private NumberSetting reach = new NumberSetting("Reach", 4.5, 3.0, 6.0, 0.1);
    private BooleanSetting waterOnly = new BooleanSetting("Water Only", false);
    
    private int cooldown = 0;

    public LiquidInteract() {
        super("LiquidInteract", "Completely reworked: Place blocks on liquids with better detection.", Category.WORLD);
        addSetting(mode);
        addSetting(requireBlock);
        addSetting(reach);
        addSetting(waterOnly);
    }

    @Override
    public void onTick() {
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Check if player is trying to interact
        boolean isUsing = mc.options.useKey.isPressed();
        boolean isAttacking = mc.options.attackKey.isPressed();
        
        if (!isUsing && !isAttacking) return;

        String currentMode = mode.getMode();
        if (currentMode.equals("Place") && !isUsing) return;
        if (currentMode.equals("Click") && !isAttacking) return;

        // Check if we have a block item (if required) - only for Place/Both modes
        if (requireBlock.isEnabled() && !currentMode.equals("Click")) {
            ItemStack mainHand = mc.player.getMainHandStack();
            ItemStack offHand = mc.player.getOffHandStack();
            if (!(mainHand.getItem() instanceof BlockItem) && !(offHand.getItem() instanceof BlockItem)) {
                return;
            }
        }

        // Perform raycast to find liquids
        Vec3d start = mc.player.getCameraPosVec(1.0f);
        Vec3d direction = mc.player.getRotationVec(1.0f);
        Vec3d end = start.add(direction.multiply(reach.getValue()));

        HitResult hit = mc.world.raycast(new RaycastContext(
            start, end,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.ANY,  // Include fluids in raycast
            mc.player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) return;
        
        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos hitPos = blockHit.getBlockPos();
        FluidState fluidState = mc.world.getFluidState(hitPos);
        BlockState blockState = mc.world.getBlockState(hitPos);

        // Check if we hit a liquid
        boolean isLiquid = !fluidState.isEmpty() || blockState.getBlock() instanceof FluidBlock;
        if (!isLiquid) return;

        // Check if water only mode is enabled
        if (waterOnly.isEnabled()) {
            String fluidName = fluidState.getFluid().toString();
            if (!fluidName.contains("water")) return;
        }

        // Try to place block or interact based on mode
        if (currentMode.equals("Place") && isUsing) {
            placeBlockOnLiquid(mc, blockHit, hitPos);
        } else if (currentMode.equals("Click") && isAttacking) {
            // Click mode - break the liquid block (used for removing water/lava source blocks)
            mc.interactionManager.attackBlock(hitPos, blockHit.getSide());
            mc.player.swingHand(Hand.MAIN_HAND);
            cooldown = 4;
        } else if (currentMode.equals("Both")) {
            if (isUsing) {
                placeBlockOnLiquid(mc, blockHit, hitPos);
            } else if (isAttacking) {
                mc.interactionManager.attackBlock(hitPos, blockHit.getSide());
                mc.player.swingHand(Hand.MAIN_HAND);
                cooldown = 4;
            }
        }
    }
    
    private void placeBlockOnLiquid(MinecraftClient mc, BlockHitResult blockHit, BlockPos hitPos) {
        Hand hand = Hand.MAIN_HAND;
        ItemStack handStack = mc.player.getMainHandStack();
        
        if (!(handStack.getItem() instanceof BlockItem)) {
            handStack = mc.player.getOffHandStack();
            hand = Hand.OFF_HAND;
            if (!(handStack.getItem() instanceof BlockItem)) {
                return;
            }
        }

        // Create a proper block hit result for placement
        Direction side = blockHit.getSide();
        Vec3d hitVec = blockHit.getPos();
        BlockHitResult placeHit = new BlockHitResult(hitVec, side, hitPos, false);
        
        mc.interactionManager.interactBlock(mc.player, hand, placeHit);
        mc.player.swingHand(hand);
        cooldown = 4;
    }
}
