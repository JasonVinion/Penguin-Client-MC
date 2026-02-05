package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoTunnel extends Module {
    private NumberSetting delay = new NumberSetting("Delay", 1.0, 0.0, 10.0, 1.0);
    private int timer = 0;

    public AutoTunnel() {
        super("AutoTunnel", "Automatically mines a 1x2 tunnel in the direction you are facing.", Category.WORLD);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (timer > 0) {
            timer--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Direction dir = mc.player.getHorizontalFacing();
        // Check 1 block ahead
        BlockPos head = mc.player.getBlockPos().offset(dir).up();
        BlockPos feet = mc.player.getBlockPos().offset(dir);

        boolean mining = false;

        if (!mc.world.isAir(head)) {
            mine(head);
            mining = true;
        } else if (!mc.world.isAir(feet)) {
            mine(feet);
            mining = true;
        }

        if (!mining) {
            // Walk forward
            mc.options.forwardKey.setPressed(true);
        } else {
            timer = (int) delay.getValue();
        }
    }

    private void mine(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.options.forwardKey.setPressed(false);
        mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public void onDisable() {
        if (MinecraftClient.getInstance().options != null) {
            MinecraftClient.getInstance().options.forwardKey.setPressed(false);
        }
    }
}
