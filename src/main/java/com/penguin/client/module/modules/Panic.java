package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;

public class Panic extends Module {
    public Panic() {
        super("Panic", "Instantly disables all modules. Emergency toggle for safety.", Category.MISC);
    }

    @Override
    public void onEnable() {
        for (Module m : ModuleManager.INSTANCE.getModules()) {
            if (m.isEnabled() && m != this) m.toggle();
        }
        this.toggle(); // Disable self
    }
}
