package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.StringSetting;

public class ChatSuffix extends Module {
    public static boolean enabled = false;
    public static StringSetting suffix = new StringSetting("Suffix", " | Penguin Client");

    public ChatSuffix() {
        super("ChatSuffix", "Appends a custom suffix to your chat messages.", Category.MISC);
        addSetting(suffix);
    }

    @Override
    public void onEnable() {
        enabled = true;
    }

    @Override
    public void onDisable() {
        enabled = false;
    }
}
