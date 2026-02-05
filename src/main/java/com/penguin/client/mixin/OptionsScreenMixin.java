package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.ui.screen.ModKeybindSettingsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add Penguin Client keybind settings button to Minecraft options screen.
 */
@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {

    protected OptionsScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addPenguinKeybindButton(CallbackInfo ci) {
        // Add a small button at the bottom of the screen
        int buttonWidth = 150;
        int buttonHeight = 20;
        int x = this.width / 2 - buttonWidth / 2;
        int y = this.height - 27; // Position above "Done" button

        this.addDrawableChild(ButtonWidget.builder(
            Text.of("Penguin Client Settings"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new ModKeybindSettingsScreen((OptionsScreen)(Object)this));
                }
            }
        ).dimensions(x - 80, y, buttonWidth, buttonHeight).build());

        // Add Disable All Modules button
        this.addDrawableChild(ButtonWidget.builder(
            Text.of("Disable All Modules"),
            button -> {
                if (ModuleManager.INSTANCE != null) {
                    ModuleManager.INSTANCE.getModules().forEach(m -> {
                        if (m.isEnabled()) m.toggle();
                    });
                }
            }
        ).dimensions(x + 80, y, buttonWidth, buttonHeight).build());
    }
}
