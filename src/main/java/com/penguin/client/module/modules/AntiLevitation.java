package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

public class AntiLevitation extends Module {
    public static AntiLevitation INSTANCE;
    public AntiLevitation() {
        super("AntiLevitation", "Cancels the levitation effect from Shulkers.", Category.PLAYER);
        INSTANCE = this;
    }
}
