package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

/**
 * RainbowEnchant - Makes the enchantment glint on items rainbow colored
 */
public class RainbowEnchant extends Module { // deprecated
    public static RainbowEnchant INSTANCE;

    public RainbowEnchant() {
        super("RainbowEnchant", "Makes the enchantment glint rainbow colored.", Category.RENDER);
        INSTANCE = this;
        setVisible(false);
    }
    
    public static int getRainbowColor() {
        if (INSTANCE == null || !INSTANCE.isEnabled()) {
            return 0x8040CC; // Default purple enchantment color
        }
        
        long time = System.currentTimeMillis() % 3600000L;
        float hue = (time / 1000.0f) % 1.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | 0xFF000000;
    }
}
