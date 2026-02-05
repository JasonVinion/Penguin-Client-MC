package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

public class Breach extends Module {
    public static Breach INSTANCE;

    public Breach() {
        super("Breach", "Allows you to interact with blocks through other blocks.", Category.COMBAT);
        INSTANCE = this;
    }
}
