package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

public class SafeWalk extends Module {
    public SafeWalk() {
        super("SafeWalk", "Prevents you from walking off block edges. Crouch without holding shift.", Category.MOVEMENT);
    }
}
