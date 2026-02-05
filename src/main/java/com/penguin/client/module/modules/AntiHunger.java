package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

public class AntiHunger extends Module {
    public static boolean enabled = false;

    public AntiHunger() {
        super("AntiHunger", "Reduces the rate at which hunger depletes.", Category.PLAYER);
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
