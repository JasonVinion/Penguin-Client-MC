package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;

public class HitBox extends Module {
    public static NumberSetting expand = new NumberSetting("Expand", 0.5, 0.0, 2.0, 0.1);

    public HitBox() {
        super("HitBox", "Expands the hitbox of entities making them easier to hit.", Category.COMBAT);
        addSetting(expand);
    }
}
