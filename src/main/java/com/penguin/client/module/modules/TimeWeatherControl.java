package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Testing module that provides quick access to common commands.
 * Includes time, weather, gamemode controls and more.
 * Requires OP/Creative mode to function.
 * Part of the Testing category, only visible when Beta Tester mode is enabled.
 */
public class TimeWeatherControl extends Module {

    private final ActionSetting setDay = new ActionSetting("Set Day", () -> setTime("day"));
    private final ActionSetting setNight = new ActionSetting("Set Night", () -> setTime("night"));
    private final ActionSetting setNoon = new ActionSetting("Set Noon", () -> setTime("noon"));
    private final ActionSetting setMidnight = new ActionSetting("Set Midnight", () -> setTime("midnight"));
    
    private final ActionSetting clearWeather = new ActionSetting("Clear Weather", () -> setWeather("clear"));
    private final ActionSetting setRain = new ActionSetting("Set Rain", () -> setWeather("rain"));
    private final ActionSetting setThunder = new ActionSetting("Set Thunder", () -> setWeather("thunder"));
    
    private final ActionSetting setCreative = new ActionSetting("Creative Mode", () -> setGamemode("creative"));
    private final ActionSetting setSurvival = new ActionSetting("Survival Mode", () -> setGamemode("survival"));
    private final ActionSetting setSpectator = new ActionSetting("Spectator Mode", () -> setGamemode("spectator"));
    private final ActionSetting setAdventure = new ActionSetting("Adventure Mode", () -> setGamemode("adventure"));
    
    private final ActionSetting enableKeepInv = new ActionSetting("Enable Keep Inv", () -> runCommand("gamerule keepInventory true"));
    private final ActionSetting disableKeepInv = new ActionSetting("Disable Keep Inv", () -> runCommand("gamerule keepInventory false"));
    private final ActionSetting disableMobGriefing = new ActionSetting("No Mob Grief", () -> runCommand("gamerule mobGriefing false"));
    private final ActionSetting enableMobGriefing = new ActionSetting("Allow Mob Grief", () -> runCommand("gamerule mobGriefing true"));

    public TimeWeatherControl() {
        super("Commands", "Quick commands: time, weather, gamemode, gamerules. Requires OP.", Category.TESTING);
        addSetting(setDay);
        addSetting(setNight);
        addSetting(setNoon);
        addSetting(setMidnight);
        addSetting(clearWeather);
        addSetting(setRain);
        addSetting(setThunder);
        addSetting(setCreative);
        addSetting(setSurvival);
        addSetting(setSpectator);
        addSetting(setAdventure);
        addSetting(enableKeepInv);
        addSetting(disableKeepInv);
        addSetting(disableMobGriefing);
        addSetting(enableMobGriefing);
    }

    private void setTime(String time) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.networkHandler.sendChatCommand("time set " + time);
    }

    private void setWeather(String weather) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.networkHandler.sendChatCommand("weather " + weather);
    }

    private void setGamemode(String mode) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.networkHandler.sendChatCommand("gamemode " + mode);
    }
    
    private void runCommand(String command) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        mc.player.networkHandler.sendChatCommand(command);
    }
}
