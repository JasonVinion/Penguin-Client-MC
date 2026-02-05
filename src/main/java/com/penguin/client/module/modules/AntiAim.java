package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * AntiAim - Makes your player look crazy to confuse other players
 * Modes: Server Only (server sees crazy angles), Server + Local (you also see it)
 */
public class AntiAim extends Module { // deprecated
    public static AntiAim INSTANCE;
    
    public ModeSetting mode = new ModeSetting("Mode", "Server Only", "Server Only", "Server + Local", "Spin", "Jitter");
    private NumberSetting spinSpeed = new NumberSetting("Spin Speed", 10.0, 1.0, 50.0, 1.0);
    private NumberSetting pitchOffset = new NumberSetting("Pitch Offset", 90.0, -90.0, 90.0, 5.0);
    private BooleanSetting randomize = new BooleanSetting("Randomize", false);
    
    private float targetYaw = 0;
    private float targetPitch = 0;
    private float serverYaw = 0;
    private float serverPitch = 0;

    public AntiAim() {
        super("AntiAim", "Makes you look crazy to other players. Server Only keeps your local view normal.", Category.COMBAT);
        addSetting(mode);
        addSetting(spinSpeed);
        addSetting(pitchOffset);
        addSetting(randomize);
        INSTANCE = this;
        setVisible(false);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String currentMode = mode.getMode();
        
        switch (currentMode) {
            case "Spin":
                // Continuous spinning
                targetYaw += (float) spinSpeed.getValue();
                if (targetYaw >= 360) targetYaw -= 360;
                targetPitch = (float) pitchOffset.getValue();
                break;
                
            case "Jitter":
                // Rapid jittering
                if (randomize.isEnabled()) {
                    targetYaw = (float) (Math.random() * 360);
                    targetPitch = (float) ((Math.random() * 180) - 90);
                } else {
                    targetYaw = (targetYaw + 180) % 360;
                    targetPitch = -targetPitch;
                    if (Math.abs(targetPitch) < 1) targetPitch = (float) pitchOffset.getValue();
                }
                break;
                
            case "Server Only":
            case "Server + Local":
                // Random angles each tick
                if (randomize.isEnabled()) {
                    targetYaw = (float) (Math.random() * 360);
                    targetPitch = (float) ((Math.random() * 180) - 90);
                } else {
                    targetYaw += (float) (spinSpeed.getValue() * 2);
                    if (targetYaw >= 360) targetYaw -= 360;
                    // Alternate pitch
                    targetPitch = targetPitch > 0 ? (float) -pitchOffset.getValue() : (float) pitchOffset.getValue();
                }
                break;
        }
        
        // Store server rotation values for use by mixin
        // Note: In "Server + Local" mode, the player's rotation is set directly above,
        // so the serverYaw/serverPitch are not used by getServerYaw/getServerPitch methods.
        // However, we still update them for consistency and potential future use.
        serverYaw = targetYaw;
        serverPitch = targetPitch;
        
        // If Server + Local mode, also affect local view (player's actual rotation)
        // The packets will naturally contain this rotation since we set it on the player
        if (currentMode.equals("Server + Local")) {
            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);
        }
    }
    
    /**
     * Called from mixin to modify rotation packets sent to server.
     * Only applies for modes where we want server to see different rotation than local.
     */
    public static float getServerYaw(float originalYaw) {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            String currentMode = INSTANCE.mode.getMode();
            // Apply server rotation for all modes except "Server + Local" which modifies player directly
            if (currentMode.equals("Server Only") || currentMode.equals("Spin") || currentMode.equals("Jitter")) {
                return INSTANCE.serverYaw;
            }
        }
        return originalYaw;
    }
    
    /**
     * Called from mixin to modify rotation packets sent to server
     */
    public static float getServerPitch(float originalPitch) {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            String currentMode = INSTANCE.mode.getMode();
            // Apply server rotation for all modes except "Server + Local" which modifies player directly
            if (currentMode.equals("Server Only") || currentMode.equals("Spin") || currentMode.equals("Jitter")) {
                return INSTANCE.serverPitch;
            }
        }
        return originalPitch;
    }
}
