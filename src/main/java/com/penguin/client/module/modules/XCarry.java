package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

public class XCarry extends Module {
    public static XCarry INSTANCE;
    public XCarry() {
        super("XCarry", "Allows storing items in the 2x2 crafting grid without dropping on close.", Category.PLAYER);
        INSTANCE = this;
    }
}
