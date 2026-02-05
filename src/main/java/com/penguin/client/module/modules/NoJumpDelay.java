package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

public class NoJumpDelay extends Module {
    public static NoJumpDelay INSTANCE;

    public NoJumpDelay() {
        super("NoJumpDelay", "Removes the delay between jumps, allowing you to jump continuously.", Category.MOVEMENT);
        INSTANCE = this;
    }
}
