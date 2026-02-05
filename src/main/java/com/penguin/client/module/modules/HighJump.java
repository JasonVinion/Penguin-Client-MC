package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;

public class HighJump extends Module {
    public static HighJump INSTANCE;
    public NumberSetting height = new NumberSetting("Height", 0.42, 0.42, 5.0, 0.1);

    public HighJump() {
        super("HighJump", "Increases jump height. Configurable boost amount.", Category.MOVEMENT);
        addSetting(height);
        INSTANCE = this;
    }

}
