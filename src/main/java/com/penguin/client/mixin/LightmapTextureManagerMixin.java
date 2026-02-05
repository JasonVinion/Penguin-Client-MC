package com.penguin.client.mixin;

import com.penguin.client.module.modules.XRay;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void redirectSetColor(NativeImage instance, int x, int y, int color) {
        if (XRay.isEnabledStatic()) {
            instance.setColor(x, y, 0xFFFFFFFF);
        } else {
            instance.setColor(x, y, color);
        }
    }
}
