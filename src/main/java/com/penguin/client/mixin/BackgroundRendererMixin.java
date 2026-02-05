package com.penguin.client.mixin;

import com.penguin.client.module.modules.AntiEffect;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @org.spongepowered.asm.mixin.injection.Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
    private static void onApplyFog(net.minecraft.client.render.Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        com.penguin.client.module.modules.AntiUnderwaterFog mod = com.penguin.client.module.ModuleManager.INSTANCE.getModule(com.penguin.client.module.modules.AntiUnderwaterFog.class);
        if (mod != null && mod.isEnabled()) {
             if (camera.getSubmersionType() == net.minecraft.client.render.CameraSubmersionType.WATER) {
                 ci.cancel();
             }
        }
    }

    @Redirect(method = "applyFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"))
    private static boolean redirectHasStatusEffect(LivingEntity entity, StatusEffect effect) {
        if (AntiEffect.INSTANCE != null && AntiEffect.INSTANCE.isEnabled() && AntiEffect.INSTANCE.noBlindness.isEnabled()) {
            if (effect == StatusEffects.BLINDNESS || effect == StatusEffects.DARKNESS) {
                return false;
            }
        }
        return entity.hasStatusEffect(effect);
    }
}
