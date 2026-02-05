package com.penguin.client.mixin;

import com.penguin.client.module.modules.IceSpeed;
import com.penguin.client.module.modules.NoSlow;
import com.penguin.client.module.modules.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void onShouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos, CallbackInfoReturnable<Boolean> info) {
        if (XRay.isEnabledStatic()) {
            if (XRay.isVisible(state.getBlock())) {
                info.setReturnValue(true);
            } else {
                info.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getVelocityMultiplier()F", at = @At("HEAD"), cancellable = true)
    private void onGetVelocityMultiplier(CallbackInfoReturnable<Float> info) {
        if (NoSlow.isEnabledStatic()) {
            if ((Object) this == Blocks.SOUL_SAND) {
                info.setReturnValue(1.0f);
            }
        }
    }
    
    @Inject(method = "getSlipperiness()F", at = @At("HEAD"), cancellable = true)
    private void onGetSlipperiness(CallbackInfoReturnable<Float> info) {
        // Apply IceSpeed modification for ice blocks
        if (IceSpeed.INSTANCE != null && IceSpeed.INSTANCE.isEnabled()) {
            Block block = (Block) (Object) this;
            if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE) {
                info.setReturnValue(IceSpeed.getIceSlipperiness());
            }
        }
    }
}
