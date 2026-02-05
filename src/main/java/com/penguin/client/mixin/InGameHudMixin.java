package com.penguin.client.mixin;

import com.penguin.client.module.modules.AntiEffect;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusEffectOverlay(DrawContext context, CallbackInfo ci) {
        if (AntiEffect.INSTANCE != null && AntiEffect.INSTANCE.isEnabled()) {
            ci.cancel();
        }
    }
}
