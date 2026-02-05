package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.StringSetting;
import net.minecraft.client.MinecraftClient;

public class Spammer extends Module {
    private int timer = 0;
    private StringSetting message = new StringSetting("Message", "Penguin Client on top!");
    private NumberSetting delay = new NumberSetting("Delay", 100, 10, 1000, 10);

    public Spammer() {
        super("Spammer", "Sends repeated messages to chat with configurable delay.", Category.MISC);
        addSetting(message);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        timer++;
        if (timer > delay.getValue()) {
            mc.player.networkHandler.sendChatMessage(message.getValue());
            timer = 0;
        }
    }
}
