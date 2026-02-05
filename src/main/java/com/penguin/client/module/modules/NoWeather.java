package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

public class NoWeather extends Module {
    private static NoWeather INSTANCE;

    public NoWeather() {
        super("NoWeather", "Disables weather effects like rain and thunderstorms.", Category.RENDER);
        INSTANCE = this;
    }

    public static boolean isEnabledStatic() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
}
