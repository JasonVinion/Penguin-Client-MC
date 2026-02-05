package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.AntiLevitation;
import com.penguin.client.module.modules.ArrowDmg;
import com.penguin.client.module.modules.HighJump;
import com.penguin.client.module.modules.LongJump;
import com.penguin.client.module.modules.NoJumpDelay;
import com.penguin.client.module.modules.PowderSnowWalk;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow private int jumpingCooldown;
    
    @Shadow public abstract ItemStack getActiveItem();

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            if (NoJumpDelay.INSTANCE != null && NoJumpDelay.INSTANCE.isEnabled()) {
                jumpingCooldown = 0;
            }
        }
    }

    @Inject(method = "getJumpVelocity", at = @At("HEAD"), cancellable = true)
    private void onGetJumpVelocity(CallbackInfoReturnable<Float> info) {
        if ((Object) this instanceof ClientPlayerEntity) {
            if (HighJump.INSTANCE != null && HighJump.INSTANCE.isEnabled()) {
                info.setReturnValue((float) HighJump.INSTANCE.height.getValue());
            }
        }
    }

    @Inject(method = "jump", at = @At("RETURN"))
    private void onJump(CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            LongJump.onJump((ClientPlayerEntity) (Object) this);
        }
    }

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void onHasStatusEffect(StatusEffect effect, CallbackInfoReturnable<Boolean> info) {
        if (effect == StatusEffects.LEVITATION) {
            if (AntiLevitation.INSTANCE != null && AntiLevitation.INSTANCE.isEnabled()) {
                info.setReturnValue(false);
            }
        }
    }
    
    /**
     * Hook for ArrowDmg - called when player stops using an item (releases bow/trident)
     */
    @Inject(method = "stopUsingItem", at = @At("HEAD"))
    private void onStopUsingItem(CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity) {
            ItemStack activeItem = getActiveItem();
            if (!activeItem.isEmpty() && ArrowDmg.INSTANCE != null && ArrowDmg.INSTANCE.isEnabled()) {
                ArrowDmg.INSTANCE.onStopUsingItem(activeItem.getItem());
            }
        }
    }

}
