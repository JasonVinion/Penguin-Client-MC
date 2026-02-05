package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.AntiWaterPush;
import com.penguin.client.module.modules.HitBox;
import com.penguin.client.module.modules.NoSlow;
import com.penguin.client.module.modules.AntiEntityPush;
import com.penguin.client.module.modules.PortalGui;
import com.penguin.client.module.modules.SafeWalk;
import com.penguin.client.module.modules.TridentFly;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Inject(method = "isTouchingWaterOrRain", at = @At("HEAD"), cancellable = true)
    private void onIsTouchingWaterOrRain(CallbackInfoReturnable<Boolean> info) {
        if ((Object) this instanceof ClientPlayerEntity) {
            if (TridentFly.INSTANCE != null && TridentFly.INSTANCE.isEnabled()) {
                info.setReturnValue(true);
            }
        }
    }

    @Inject(method = "updateMovementInFluid", at = @At("HEAD"), cancellable = true)
    private void onUpdateMovementInFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> info) {
        AntiWaterPush module = ModuleManager.INSTANCE.getModule(AntiWaterPush.class);
        if (module != null && module.isEnabled()) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> info) {
        HitBox hitBox = ModuleManager.INSTANCE.getModule(HitBox.class);
        if (hitBox != null && hitBox.isEnabled()) {
            info.setReturnValue((float) HitBox.expand.getValue());
        }
    }

    @Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
    private void onSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci) {
        if (NoSlow.isEnabledStatic()) {
            if (state.getBlock() == Blocks.COBWEB || state.getBlock() == Blocks.CACTUS || state.getBlock() == Blocks.SWEET_BERRY_BUSH) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
        AntiEntityPush module = ModuleManager.INSTANCE.getModule(AntiEntityPush.class);
        if (module != null && module.isEnabled()) {
            ci.cancel();
        }
    }
    
    /**
     * Allows opening GUIs while in a portal by returning 0 for portal time
     */
    @Inject(method = "getPortalCooldown", at = @At("HEAD"), cancellable = true)
    private void onGetPortalCooldown(CallbackInfoReturnable<Integer> info) {
        if ((Object) this instanceof ClientPlayerEntity) {
            if (PortalGui.shouldAllowGui()) {
                info.setReturnValue(0);
            }
        }
    }
}
