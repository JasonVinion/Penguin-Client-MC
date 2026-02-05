package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.MovementUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

/**
 * BoatFly module - Various boat flight and phase capabilities.
 * Fly: Standard boat flight
 * Phase: Phase through blocks with boat (noClip enabled)
 * Glitch: Glitch boat into blocks when dismounting
 */
public class BoatFly extends Module {
    public static BoatFly INSTANCE;

    private ModeSetting mode = new ModeSetting("Mode", "Fly", "Fly", "Phase", "Glitch");
    private NumberSetting speed = new NumberSetting("Speed", 5.0, 0.1, 50.0, 0.1);
    private NumberSetting vertical = new NumberSetting("Vertical", 2.0, 0.1, 10.0, 0.1);
    private BooleanSetting lockYaw = new BooleanSetting("Lock Yaw", true);
    private BooleanSetting fall = new BooleanSetting("Fall", false);
    private NumberSetting fallSpeed = new NumberSetting("Fall Speed", 0.625, 0.1, 5.0, 0.1);
    
    // Glitch mode variables
    private Entity glitchBoat = null;
    private int glitchTicks = 0;
    private boolean glitchActive = false;

    public BoatFly() {
        super("BoatFly", "Fly: Standard boat flight. Phase: Phase through blocks with boat. Glitch: Glitch boat into blocks when dismounting (press sneak to trigger).", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
        addSetting(vertical);
        addSetting(lockYaw);
        addSetting(fall);
        addSetting(fallSpeed);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        glitchBoat = null;
        glitchTicks = 0;
        glitchActive = false;
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (glitchBoat != null) {
            glitchBoat.noClip = false;
            glitchBoat = null;
        }
        // Re-enable boat collision when disabled
        if (mc.player != null && mc.player.getVehicle() instanceof BoatEntity boat) {
            boat.noClip = false;
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        String currentMode = mode.getMode();

        if (currentMode.equals("Glitch")) {
            handleGlitchMode(mc);
            return;
        }

        if (!mc.player.hasVehicle()) return;
        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof BoatEntity boat)) return;

        if (currentMode.equals("Phase")) {
            // Phase mode - enable noClip on boat
            boat.noClip = true;
        }

        // Lock yaw to player's facing direction
        if (lockYaw.isEnabled()) {
            boat.setYaw(mc.player.getYaw());
        }

        Vec3d vel = vehicle.getVelocity();
        double motionX = vel.x;
        double motionY = 0;
        double motionZ = vel.z;

        // Horizontal movement
        if (MovementUtils.isMoving()) {
            double dir = MovementUtils.getDirection();
            motionX = -Math.sin(dir) * speed.getValue();
            motionZ = Math.cos(dir) * speed.getValue();
        } else {
            motionX = 0;
            motionZ = 0;
        }

        // Vertical movement
        if (mc.options.jumpKey.isPressed()) {
            motionY = vertical.getValue();
        } else if (mc.options.sprintKey.isPressed()) {
            motionY = -vertical.getValue();
        } else if (fall.isEnabled()) {
            motionY = -fallSpeed.getValue() / 20.0;
        }

        vehicle.setVelocity(motionX, motionY, motionZ);
    }

    private void handleGlitchMode(MinecraftClient mc) {
        // Handle glitch countdown
        if (glitchTicks > 0) {
            glitchTicks--;
            if (glitchTicks == 0 && glitchBoat != null) {
                glitchBoat.noClip = false;
                glitchActive = false;
            }
        }

        if (!mc.player.hasVehicle()) return;
        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof BoatEntity boat)) return;

        // Check for sneak to trigger glitch
        if (mc.options.sneakKey.isPressed() && !glitchActive) {
            glitchActive = true;
            glitchBoat = boat;
            boat.noClip = true;
            glitchTicks = 5; // Boat will phase for 5 ticks then stop
        }

        // Still allow basic movement when not glitching
        if (!glitchActive) {
            if (lockYaw.isEnabled()) {
                boat.setYaw(mc.player.getYaw());
            }

            Vec3d vel = vehicle.getVelocity();
            double motionX = 0;
            double motionY = 0;
            double motionZ = 0;

            if (MovementUtils.isMoving()) {
                double dir = MovementUtils.getDirection();
                motionX = -Math.sin(dir) * speed.getValue();
                motionZ = Math.cos(dir) * speed.getValue();
            }

            if (mc.options.jumpKey.isPressed()) {
                motionY = vertical.getValue();
            } else if (mc.options.sprintKey.isPressed()) {
                motionY = -vertical.getValue();
            }

            vehicle.setVelocity(motionX, motionY, motionZ);
        }
    }
}
