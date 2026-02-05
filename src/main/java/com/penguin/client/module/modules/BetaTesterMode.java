package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

/**
 * Beta Tester Mode module - enables access to the Testing category
 * with developer/testing modules. When enabled, the Testing category
 * becomes visible in the menu.
 */
public class BetaTesterMode extends Module {
    public static BetaTesterMode INSTANCE;

    public BetaTesterMode() {
        super("BetaTesterMode", 
              "Enable beta tester features. Shows the Testing category with developer modules that require OP/Creative mode.",
              Category.SETTINGS);
        INSTANCE = this;
    }

    /**
     * Returns true if beta tester mode is enabled.
     */
    public boolean isBetaTesterEnabled() {
        return isEnabled();
    }

    @Override
    public void onEnable() {
        // Nothing special on enable - the Testing category visibility
        // is handled by Category.isVisible() checking this module's state
    }

    @Override
    public void onDisable() {
        // Nothing special on disable
    }
}
