package com.penguin.client.mixin;

import com.penguin.client.module.modules.Zoom;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Zoom.isEnabledStatic() && Zoom.scrollControl.isEnabled()) {
            // Cancel normal scroll behavior and adjust zoom
            Zoom.adjustZoom(vertical);
            ci.cancel();
        }
    }
}
