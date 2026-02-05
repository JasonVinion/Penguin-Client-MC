package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.Breach;
import com.penguin.client.module.modules.Reach;
import com.penguin.client.module.modules.Criticals;
import com.penguin.client.module.modules.SprintReset;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "getReachDistance", at = @At("HEAD"), cancellable = true)
    private void onGetReachDistance(CallbackInfoReturnable<Float> info) {
        Reach reach = ModuleManager.INSTANCE.getModule(Reach.class);
        if (reach != null && reach.isEnabled()) {
            info.setReturnValue((float) Reach.distance.getValue());
        }

        Breach breach = ModuleManager.INSTANCE.getModule(Breach.class);
        if (breach != null && breach.isEnabled()) {
            // Breach often implies longer reach too, or just ignoring walls.
            // If we just increase reach, it might not ignore walls depending on raytrace logic.
            // But usually reach bypasses wall check distance if server allows.
            // However, true "Interact Through Walls" often requires ignoring the block hit result in raycast.
            // That's usually in MinecraftClient.doItemUse or similar.
            // But ensuring reach is sufficient is step 1.
            if (reach == null || !reach.isEnabled()) {
                 info.setReturnValue(5.0f); // Default breach reach?
            }
        }
    }

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        Criticals.onAttack(target);
        SprintReset.onAttack();
    }
}
