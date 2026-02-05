package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;

public class Flight extends Module {
    public static Flight INSTANCE;

    private ModeSetting mode = new ModeSetting("Mode", "Standard", "Standard", "Bob", "Anti-Kick", "Paper-Kick");
    private BooleanSetting noFallDisable = new BooleanSetting("NoFall on Disable", true);
    private NumberSetting speed = new NumberSetting("Speed", 0.3, 0.1, 2.0, 0.1);
    private BooleanSetting verticalSpeedMatch = new BooleanSetting("Vertical Match", false);
    private NumberSetting bobSpeed = new NumberSetting("Bob Speed", 10.0, 5.0, 40.0, 1.0);
    private NumberSetting bobHeight = new NumberSetting("Bob Height", 0.15, 0.05, 0.5, 0.01);
    
    private int antiKickTimer = 0;
    private int bobTimer = 0;
    private boolean bobUp = true;
    private int floatingTicks = 0;

    public Flight() {
        super("Flight", "Allows you to fly. Standard: Basic flight. Anti-Kick: Basic anti-kick. Paper-Kick: Advanced anti-kick for Paper servers. Bob: Smooth bobbing motion.", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
        addSetting(verticalSpeedMatch);
        addSetting(noFallDisable);
        addSetting(bobSpeed);
        addSetting(bobHeight);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        bobTimer = 0;
        bobUp = true;
        antiKickTimer = 0;
        floatingTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        mc.player.getAbilities().flying = true;

        String currentMode = mode.getMode();

        if (currentMode.equals("Bob")) {
            // Bob mode - make the player bob up and down smoothly
            bobTimer++;
            int bobSpeedVal = (int) bobSpeed.getValue();
            double height = bobHeight.getValue();
            
            if (bobTimer >= bobSpeedVal) {
                bobTimer = 0;
                bobUp = !bobUp;
            }
            
            double bobVelocity = bobUp ? height : -height;
            mc.player.setVelocity(mc.player.getVelocity().x, bobVelocity, mc.player.getVelocity().z);
            
        } else if (currentMode.equals("Anti-Kick")) {
            // Anti-Kick mode - moves the player down by at least 0.04m every two seconds
            antiKickTimer++;
            
            if (antiKickTimer == 40) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.04, mc.player.getVelocity().z);
            } else if (antiKickTimer >= 50) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.04, mc.player.getVelocity().z);
                antiKickTimer = 0;
            }
        } else if (currentMode.equals("Paper-Kick")) {
            // Paper anti-kick mode - more aggressive anti-kick for Paper servers
            // Send packets to ground periodically to avoid floating detection
            floatingTicks++;
            
            if (floatingTicks >= 20) {
                Box box = mc.player.getBoundingBox();
                Box adjustedBox = box.offset(0, -0.4, 0);
                
                // Check if there's space below to move down safely
                boolean hasSpaceBelow = !mc.world.getBlockCollisions(mc.player, adjustedBox).iterator().hasNext();
                
                if (hasSpaceBelow) {
                    // Send down and back up packets
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY() - 0.4, mc.player.getZ(), mc.player.isOnGround()
                    ));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()
                    ));
                }
                floatingTicks = 0;
            }
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;

            if (noFallDisable.isEnabled()) {
                // Enable the actual NoFall module temporarily or apply comprehensive protection
                NoFall noFallModule = ModuleManager.INSTANCE.getModule(NoFall.class);
                
                // Reset fall distance immediately
                mc.player.fallDistance = 0;
                
                // Send multiple ground packets to ensure server accepts it
                for (int i = 0; i < 3; i++) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                }
                
                // Also send position packet with onGround = true
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(), true
                ));
                
                // Enable NoFall module if it exists and isn't already enabled
                if (noFallModule != null && !noFallModule.isEnabled()) {
                    noFallModule.toggle();
                }
            }
        }
    }
}
