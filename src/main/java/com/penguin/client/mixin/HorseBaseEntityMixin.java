package com.penguin.client.mixin;

import com.penguin.client.module.modules.EntityControl;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public class HorseBaseEntityMixin {
    
    /**
     * Allow controlling horses without saddles when EntityControl is enabled
     */
    @Inject(method = "isSaddled", at = @At("HEAD"), cancellable = true)
    private void onIsSaddled(CallbackInfoReturnable<Boolean> cir) {
        if (EntityControl.canControlWithoutSaddle()) {
            cir.setReturnValue(true);
        }
    }
    
    /**
     * Allow taming horses instantly when EntityControl is enabled  
     */
    @Inject(method = "isTame", at = @At("HEAD"), cancellable = true)
    private void onIsTame(CallbackInfoReturnable<Boolean> cir) {
        if (EntityControl.canControlWithoutSaddle()) {
            cir.setReturnValue(true);
        }
    }
}
