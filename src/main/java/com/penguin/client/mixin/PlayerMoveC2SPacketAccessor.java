package com.penguin.client.mixin;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {
    @Accessor("onGround")
    void setOnGround(boolean onGround);
    
    @Accessor("yaw")
    void setYaw(float yaw);
    
    @Accessor("pitch")
    void setPitch(float pitch);
    
    @Accessor("yaw")
    float getYaw();
    
    @Accessor("pitch")
    float getPitch();
}
