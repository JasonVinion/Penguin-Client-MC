package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public class AutoDisconnect extends Module {
    public static AutoDisconnect INSTANCE;
    
    private NumberSetting health = new NumberSetting("Health", 4, 1, 20, 1);
    private BooleanSetting autoDisable = new BooleanSetting("Auto Disable", true);
    public BooleanSetting silentDisconnect = new BooleanSetting("Silent Disconnect", false);
    
    // Player proximity detection settings
    private BooleanSetting playerDetection = new BooleanSetting("Player Detection", false);
    private NumberSetting playerRange = new NumberSetting("Player Range", 50, 10, 200, 5);
    private NumberSetting playerCountThreshold = new NumberSetting("Player Count", 1, 1, 10, 1);
    private StringSetting whitelist = new StringSetting("Whitelist", "");
    
    private boolean wasInGame = false;
    private int checkCounter = 0; // Used for optimization to not check every tick
    private Set<String> whitelistCache = new HashSet<>();
    private String lastWhitelistValue = "";

    public AutoDisconnect() {
        super("AutoDisconnect", "Automatically disconnects when health drops below threshold or when players are detected nearby. Silent Disconnect: Won't show disconnect screen.", Category.MISC);
        addSetting(health);
        addSetting(autoDisable);
        addSetting(silentDisconnect);
        addSetting(playerDetection);
        addSetting(playerRange);
        addSetting(playerCountThreshold);
        addSetting(whitelist);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        wasInGame = mc.player != null && mc.world != null;
        updateWhitelistCache();
    }
    
    private void updateWhitelistCache() {
        String currentValue = whitelist.getValue();
        if (!currentValue.equals(lastWhitelistValue)) {
            whitelistCache.clear();
            if (!currentValue.isEmpty()) {
                String[] names = currentValue.split(",");
                for (String name : names) {
                    whitelistCache.add(name.trim().toLowerCase());
                }
            }
            lastWhitelistValue = currentValue;
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean inGame = mc.player != null && mc.world != null;

        if (inGame && !wasInGame) {
            // Just joined
            if (autoDisable.isEnabled() && this.isEnabled()) {
                this.toggle();
                wasInGame = inGame;
                return;
            }
        }
        wasInGame = inGame;

        if (mc.player == null || mc.world == null) return;
        
        // Health check - always performed
        if (mc.player.getHealth() < health.getValue()) {
            disconnect("AutoDisconnect: Health too low!");
            return;
        }
        
        // Player detection check - optimized to run every 5 ticks
        if (playerDetection.isEnabled()) {
            checkCounter++;
            if (checkCounter >= 5) {
                checkCounter = 0;
                updateWhitelistCache();
                checkNearbyPlayers(mc);
            }
        }
    }
    
    private void checkNearbyPlayers(MinecraftClient mc) {
        double rangeSquared = playerRange.getValue() * playerRange.getValue();
        int threshold = (int) playerCountThreshold.getValue();
        int nearbyCount = 0;
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            // Skip self
            if (player == mc.player) continue;
            
            // Skip whitelisted players
            if (whitelistCache.contains(player.getName().getString().toLowerCase())) continue;
            
            // Check distance using squared distance for performance
            double distSquared = mc.player.squaredDistanceTo(player);
            if (distSquared <= rangeSquared) {
                nearbyCount++;
                if (nearbyCount >= threshold) {
                    disconnect("AutoDisconnect: " + nearbyCount + " player(s) detected nearby!");
                    return;
                }
            }
        }
    }
    
    private void disconnect(String reason) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getConnection().disconnect(Text.of(reason));
        }
        if (this.isEnabled()) this.toggle();
    }
    
    /**
     * Check if silent disconnect is enabled
     */
    public static boolean isSilentDisconnectEnabled() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.silentDisconnect.isEnabled();
    }
}
