package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;

public class Reach extends Module {
    public static NumberSetting distance = new NumberSetting("Distance", 4.0, 3.0, 6.0, 0.1);

    public Reach() {
        super("Reach", "Extends your attack and interaction range beyond normal.", Category.COMBAT);
        addSetting(distance);
    }
}
