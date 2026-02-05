package com.penguin.client.mixin;

import com.penguin.client.module.modules.GhostMode;
import net.minecraft.client.gui.screen.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for DeathScreen to support GhostMode
 */
@Mixin(DeathScreen.class)
public class DeathScreenMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        // GhostMode - prevent death screen from showing
        if (GhostMode.INSTANCE != null && GhostMode.INSTANCE.isEnabled()) {
            GhostMode.INSTANCE.onDeathScreen();
            ci.cancel();
        }
    }
}
