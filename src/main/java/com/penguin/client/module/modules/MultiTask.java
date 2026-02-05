package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

/**
 * MultiTask - Allows you to interact with blocks/entities while eating or using items
 */
public class MultiTask extends Module { // deprecated
    public static MultiTask INSTANCE;

    public MultiTask() {
        super("MultiTask", "Allows interacting with blocks/entities while eating or using items.", Category.PLAYER);
        INSTANCE = this;
        setVisible(false);
    }
    
    public static boolean isEnabledStatic() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
}
