package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.InventoryMove;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow private InputUtil.Key boundKey;

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    private void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        InventoryMove invMove = ModuleManager.INSTANCE.getModule(InventoryMove.class);
        if (invMove != null && invMove.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen)) {
                if (this.boundKey.getCategory() == InputUtil.Type.KEYSYM) {
                     boolean pressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), this.boundKey.getCode());
                     cir.setReturnValue(pressed);
                }
            }
        }
    }
}
