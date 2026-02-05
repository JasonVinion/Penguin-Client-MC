package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.InfiniteChat;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Shadow protected TextFieldWidget chatField;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        InfiniteChat module = ModuleManager.INSTANCE.getModule(InfiniteChat.class);
        if (module != null && module.isEnabled() && chatField != null) {
            chatField.setMaxLength(2048);
        }
    }
}
