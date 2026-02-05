package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that removes entities in a configurable radius.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class EntityKill extends Module {
    
    private final NumberSetting radius = new NumberSetting("Radius", 50, 1, 200, 5);
    private final BooleanSetting includePlayers = new BooleanSetting("Include Players", false);
    
    private final ActionSetting killAllMobs = new ActionSetting("Kill All Mobs", this::killAllMobs);
    private final ActionSetting killHostileMobs = new ActionSetting("Kill Hostile Mobs", this::killHostileMobs);
    private final ActionSetting killPassiveMobs = new ActionSetting("Kill Passive Mobs", this::killPassiveMobs);
    private final ActionSetting killAllEntities = new ActionSetting("Kill All Entities", this::killAllEntities);
    private final ActionSetting killItems = new ActionSetting("Kill Items", this::killItems);
    private final ActionSetting killProjectiles = new ActionSetting("Kill Projectiles", this::killProjectiles);

    public EntityKill() {
        super("EntityKill", "Removes entities in radius. Uses /kill command. Requires OP.", Category.TESTING);
        addSetting(radius);
        addSetting(includePlayers);
        addSetting(killAllMobs);
        addSetting(killHostileMobs);
        addSetting(killPassiveMobs);
        addSetting(killAllEntities);
        addSetting(killItems);
        addSetting(killProjectiles);
    }
    
    @Override
    public void onEnable() {
        // When toggled on, kill all mobs and disable
        killAllMobs();
        this.toggle();
    }

    private void killAllMobs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int r = (int) radius.getValue();
        
        // Kill all mobs (excludes players by default)
        String command = String.format("kill @e[type=!player,type=!item,type=!experience_orb,distance=..%d]", r);
        mc.player.networkHandler.sendChatCommand(command);
    }
    
    private void killHostileMobs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int r = (int) radius.getValue();
        
        // Kill hostile mobs using type selectors for common hostiles
        String[] hostileMobs = {
            "zombie", "skeleton", "creeper", "spider", "cave_spider", "enderman", 
            "witch", "slime", "magma_cube", "blaze", "ghast", "wither_skeleton",
            "phantom", "drowned", "husk", "stray", "vindicator", "evoker", "pillager",
            "ravager", "vex", "hoglin", "piglin_brute", "zoglin", "warden", "breeze"
        };
        
        for (String mob : hostileMobs) {
            String command = String.format("kill @e[type=%s,distance=..%d]", mob, r);
            mc.player.networkHandler.sendChatCommand(command);
        }
    }
    
    private void killPassiveMobs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int r = (int) radius.getValue();
        
        // Kill passive mobs
        String[] passiveMobs = {
            "pig", "cow", "sheep", "chicken", "horse", "donkey", "mule", "rabbit",
            "villager", "iron_golem", "snow_golem", "cat", "wolf", "fox", "bee",
            "turtle", "dolphin", "squid", "glow_squid", "axolotl", "goat", "frog",
            "allay", "camel", "sniffer", "armadillo"
        };
        
        for (String mob : passiveMobs) {
            String command = String.format("kill @e[type=%s,distance=..%d]", mob, r);
            mc.player.networkHandler.sendChatCommand(command);
        }
    }
    
    private void killAllEntities() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int r = (int) radius.getValue();
        
        String command;
        if (includePlayers.isEnabled()) {
            // Kill absolutely everything including players (dangerous!)
            command = String.format("kill @e[distance=..%d]", r);
        } else {
            // Kill all entities except players
            command = String.format("kill @e[type=!player,distance=..%d]", r);
        }
        mc.player.networkHandler.sendChatCommand(command);
    }
    
    private void killItems() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int r = (int) radius.getValue();
        
        // Kill item entities and XP orbs
        String command = String.format("kill @e[type=item,distance=..%d]", r);
        mc.player.networkHandler.sendChatCommand(command);
        command = String.format("kill @e[type=experience_orb,distance=..%d]", r);
        mc.player.networkHandler.sendChatCommand(command);
    }
    
    private void killProjectiles() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int r = (int) radius.getValue();
        
        // Kill common projectiles
        String[] projectiles = {
            "arrow", "spectral_arrow", "trident", "fireball", "small_fireball",
            "dragon_fireball", "wither_skull", "shulker_bullet", "llama_spit",
            "snowball", "egg", "ender_pearl", "potion", "experience_bottle",
            "fishing_bobber", "wind_charge"
        };
        
        for (String proj : projectiles) {
            String command = String.format("kill @e[type=%s,distance=..%d]", proj, r);
            mc.player.networkHandler.sendChatCommand(command);
        }
    }
}
