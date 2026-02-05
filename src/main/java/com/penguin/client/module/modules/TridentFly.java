package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class TridentFly extends Module {
    public static TridentFly INSTANCE;
    private BooleanSetting noFallOnDisable = new BooleanSetting("NoFall on Disable", true);

    public TridentFly() {
        super("TridentFly", "Allows Riptide trident to work without water. Enable NoFall on Disable to prevent fall damage when landing.", Category.MOVEMENT);
        addSetting(noFallOnDisable);
        INSTANCE = this;
    }
    
    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && noFallOnDisable.isEnabled()) {
            // Reset fall distance
            mc.player.fallDistance = 0;
            
            // Send ground packets
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), true
            ));
            
            // Enable NoFall module if available
            NoFall noFallModule = ModuleManager.INSTANCE.getModule(NoFall.class);
            if (noFallModule != null && !noFallModule.isEnabled()) {
                noFallModule.toggle();
            }
        }
    }
}
