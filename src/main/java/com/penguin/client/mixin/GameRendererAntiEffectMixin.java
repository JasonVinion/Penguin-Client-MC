package com.penguin.client.mixin;

import com.penguin.client.module.modules.AntiEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererAntiEffectMixin {

    @Redirect(
            method = "getNightVisionStrength",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z"),
            require = 0
    )
    private static boolean redirectHasStatusEffect(LivingEntity entity, StatusEffect effect) {
        if (AntiEffect.INSTANCE != null && AntiEffect.INSTANCE.isEnabled() && AntiEffect.INSTANCE.noBlindness.isEnabled()) {
            if (effect == StatusEffects.BLINDNESS || effect == StatusEffects.DARKNESS) {
                return false;
            }
        }
        return entity.hasStatusEffect(effect);
    }
}
