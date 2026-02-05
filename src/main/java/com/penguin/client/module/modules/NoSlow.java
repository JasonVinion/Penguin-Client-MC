package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;

public class NoSlow extends Module {
    private static NoSlow INSTANCE;
    
    private BooleanSetting items = new BooleanSetting("Items", true);
    private BooleanSetting cobwebs = new BooleanSetting("Cobwebs", true);
    private BooleanSetting soulSand = new BooleanSetting("Soul Sand", true);
    private BooleanSetting slimeBlocks = new BooleanSetting("Slime Blocks", true);
    private BooleanSetting honeyBlocks = new BooleanSetting("Honey Blocks", true);
    private NumberSetting slowFactor = new NumberSetting("Slow Factor", 100, 0, 100, 5);

    public NoSlow() {
        super("NoSlow", "Prevents slowdown from using items, cobwebs, soul sand, and more.", Category.MOVEMENT);
        addSetting(items);
        addSetting(cobwebs);
        addSetting(soulSand);
        addSetting(slimeBlocks);
        addSetting(honeyBlocks);
        addSetting(slowFactor);
        INSTANCE = this;
    }

    public static boolean isEnabledStatic() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
    
    public static boolean shouldCancelItemSlow() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.items.isEnabled();
    }
    
    public static boolean shouldCancelCobwebSlow() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.cobwebs.isEnabled();
    }
    
    public static boolean shouldCancelSoulSandSlow() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.soulSand.isEnabled();
    }
    
    public static boolean shouldCancelSlimeBlockSlow() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.slimeBlocks.isEnabled();
    }
    
    public static boolean shouldCancelHoneyBlockSlow() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.honeyBlocks.isEnabled();
    }
    
    public static double getSlowFactor() {
        return INSTANCE != null ? INSTANCE.slowFactor.getValue() / 100.0 : 1.0;
    }
}
