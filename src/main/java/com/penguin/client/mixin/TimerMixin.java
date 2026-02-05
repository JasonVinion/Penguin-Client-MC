package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.Timer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class TimerMixin {

    @Shadow public float lastFrameDuration;
    
    private float originalLastFrameDuration = 0.0f;
    private boolean hasModified = false;

    @Inject(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;lastFrameDuration:F", opcode = 181, shift = At.Shift.AFTER))
    private void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        // Store the original value before any modifications
        if (!hasModified) {
            originalLastFrameDuration = this.lastFrameDuration;
        }
        
        float multiplier = 1.0f;

        Timer timer = ModuleManager.INSTANCE.getModule(Timer.class);
        if (timer != null && timer.isEnabled()) {
            multiplier *= (float) timer.speed.getValue();
        }

        if (multiplier != 1.0f) {
            this.lastFrameDuration = originalLastFrameDuration * multiplier;
            hasModified = true;
        } else {
            this.lastFrameDuration = originalLastFrameDuration;
            hasModified = false;
        }
    }
}
