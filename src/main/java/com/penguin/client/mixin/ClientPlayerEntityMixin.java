package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.MultiTask;
import com.penguin.client.module.modules.NoSlow;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean redirectIsUsingItem(ClientPlayerEntity instance) {
        // NoSlow removes the slowdown when using items
        if (NoSlow.isEnabledStatic()) {
            return false;
        }
        // MultiTask allows attacking/mining while using items
        if (MultiTask.isEnabledStatic()) {
            return false;
        }
        return instance.isUsingItem();
    }
}
