package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;

public class Velocity extends Module {
    public static NumberSetting horizontal = new NumberSetting("Horizontal", 0.0, 0.0, 100.0, 5.0);
    public static NumberSetting vertical = new NumberSetting("Vertical", 0.0, 0.0, 100.0, 5.0);
    
    private static Velocity INSTANCE;
    
    public Velocity() {
        super("Velocity", "Reduces or eliminates knockback from attacks. Adjust horizontal and vertical values.", Category.COMBAT);
        addSetting(horizontal);
        addSetting(vertical);
        INSTANCE = this;
    }
    
    public static boolean isEnabledStatic() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
    
    public static double getHorizontalMultiplier() {
        return INSTANCE != null ? INSTANCE.horizontal.getValue() / 100.0 : 1.0;
    }
    
    public static double getVerticalMultiplier() {
        return INSTANCE != null ? INSTANCE.vertical.getValue() / 100.0 : 1.0;
    }
}
