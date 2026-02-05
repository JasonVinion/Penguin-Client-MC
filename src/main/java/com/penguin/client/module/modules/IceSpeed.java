package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * IceSpeed - Move faster on ice blocks by reducing slipperiness.
 * Lower slipperiness = faster acceleration on ice.
 * Normal ice slipperiness is 0.98, we reduce it to speed up.
 */
public class IceSpeed extends Module {
    public static IceSpeed INSTANCE;
    
    // Speed multiplier: higher value = lower slipperiness = faster movement
    private NumberSetting speed = new NumberSetting("Speed", 1.5, 1.0, 3.0, 0.1);

    public IceSpeed() {
        super("IceSpeed", "Move faster on ice blocks by reducing slipperiness.", Category.MOVEMENT);
        addSetting(speed);
        INSTANCE = this;
    }
    
    /**
     * Returns modified slipperiness for ice blocks.
     * Normal ice slipperiness is 0.98. We divide by speed to reduce it.
     * e.g., speed=1.5 -> 0.98/1.5 = 0.653 (faster acceleration)
     */
    public static float getIceSlipperiness() {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            // Divide slipperiness by speed factor to reduce friction
            float modified = 0.98f / (float) INSTANCE.speed.getValue();
            // Clamp to reasonable range (0.4 to 0.98)
            return Math.max(0.4f, Math.min(modified, 0.98f));
        }
        return 0.98f;
    }
}
