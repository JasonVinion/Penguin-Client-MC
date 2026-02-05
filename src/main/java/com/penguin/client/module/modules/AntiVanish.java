package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.*;

/**
 * AntiVanish module - Notifies user when an admin uses /vanish.
 * Works by detecting players who leave without a quit message.
 * Ported from meteor-rejects, adapted for 1.20.1
 */
public class AntiVanish extends Module {
    public static AntiVanish INSTANCE;

    public NumberSetting interval = new NumberSetting("Interval", 100, 20, 300, 10);
    public ModeSetting mode = new ModeSetting("Mode", "LeaveMessage", "LeaveMessage", "TabList");
    
    private Map<UUID, String> playerCache = new HashMap<>();
    private List<String> messageCache = new ArrayList<>();
    private int timer = 0;

    public AntiVanish() {
        super("AntiVanish", "Notifies when an admin uses /vanish. LeaveMessage mode checks for missing quit messages. TabList mode tracks tab list changes.", Category.MISC);
        addSetting(interval);
        addSetting(mode);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        timer = 0;
        messageCache.clear();
        playerCache.clear();
        
        // Initialize player cache
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            for (PlayerListEntry entry : mc.player.networkHandler.getPlayerList()) {
                if (entry.getProfile() != null) {
                    playerCache.put(entry.getProfile().getId(), entry.getProfile().getName());
                }
            }
        }
    }

    /**
     * Called from mixin when a chat message is received
     */
    public void onReceiveMessage(String message) {
        messageCache.add(message);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.networkHandler == null) return;

        timer++;
        if (timer < interval.getValue()) return;

        String currentMode = mode.getMode();
        
        if (currentMode.equals("LeaveMessage")) {
            // Copy old player cache
            Map<UUID, String> oldPlayers = new HashMap<>(playerCache);
            
            // Update player cache with current players
            playerCache.clear();
            for (PlayerListEntry entry : mc.player.networkHandler.getPlayerList()) {
                if (entry.getProfile() != null) {
                    playerCache.put(entry.getProfile().getId(), entry.getProfile().getName());
                }
            }

            // Check for players who left without a message
            for (UUID uuid : oldPlayers.keySet()) {
                if (playerCache.containsKey(uuid)) continue;
                
                String name = oldPlayers.get(uuid);
                if (name.contains(" ")) continue;
                if (name.length() < 3 || name.length() > 16) continue;
                
                // Check if any recent message mentions this player (quit message)
                boolean hasQuitMessage = false;
                for (String msg : messageCache) {
                    if (msg.contains(name)) {
                        hasQuitMessage = true;
                        break;
                    }
                }
                
                if (!hasQuitMessage) {
                    // Player left without quit message - likely vanished
                    sendVanishWarning(name);
                }
            }
        } else if (currentMode.equals("TabList")) {
            // Simple tab list tracking mode
            Set<UUID> currentPlayers = new HashSet<>();
            for (PlayerListEntry entry : mc.player.networkHandler.getPlayerList()) {
                if (entry.getProfile() != null) {
                    currentPlayers.add(entry.getProfile().getId());
                }
            }
            
            // Check for missing players
            for (UUID uuid : playerCache.keySet()) {
                if (!currentPlayers.contains(uuid)) {
                    String name = playerCache.get(uuid);
                    if (name != null && !name.contains(" ") && name.length() >= 3 && name.length() <= 16) {
                        sendVanishWarning(name);
                    }
                }
            }
            
            // Update cache
            playerCache.clear();
            for (PlayerListEntry entry : mc.player.networkHandler.getPlayerList()) {
                if (entry.getProfile() != null) {
                    playerCache.put(entry.getProfile().getId(), entry.getProfile().getName());
                }
            }
        }

        timer = 0;
        messageCache.clear();
    }

    private void sendVanishWarning(String playerName) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(
                net.minecraft.text.Text.of("§c[AntiVanish] §f" + playerName + " has gone into vanish."), 
                false
            );
        }
    }
}
