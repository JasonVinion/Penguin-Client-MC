package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.MovementUtils;
import net.minecraft.client.network.ClientPlayerEntity;

public class LongJump extends Module {
    public static LongJump INSTANCE;
    public NumberSetting boost = new NumberSetting("Boost", 0.5, 0.1, 3.0, 0.1);

    public LongJump() {
        super("LongJump", "Propels you much farther when jumping. Useful for bridging gaps.", Category.MOVEMENT);
        addSetting(boost);
        INSTANCE = this;
    }

    public static void onJump(ClientPlayerEntity player) {
        if (INSTANCE != null && INSTANCE.isEnabled() && MovementUtils.isMoving()) {
            double dir = MovementUtils.getDirection();
            double boostVal = INSTANCE.boost.getValue();
            player.setVelocity(player.getVelocity().add(
                -Math.sin(dir) * boostVal,
                0,
                Math.cos(dir) * boostVal
            ));
        }
    }
}
