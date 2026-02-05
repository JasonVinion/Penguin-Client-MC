package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;

public class Timer extends Module {
    public NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 10.0, 0.1);

    public Timer() {
        super("Timer", "Changes the client tick speed.", Category.WORLD);
        addSetting(speed);
    }
}
