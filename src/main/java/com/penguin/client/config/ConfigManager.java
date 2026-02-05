package com.penguin.client.config;

import com.google.gson.*;
import com.penguin.client.logging.StartupLogger;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.settings.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Manages saving and loading of client configuration including module states,
 * settings, keybindings, and profiles.
 */
public class ConfigManager {
    public static final ConfigManager INSTANCE = new ConfigManager();
    
    private static final String CONFIG_DIR = "penguin-client";
    private static final String MAIN_CONFIG = "config.json";
    private static final String PROFILES_DIR = "profiles";
    private static final String DEFAULT_PROFILE = "default";
    
    private Path configPath;
    private Path profilesPath;
    private String currentProfile = DEFAULT_PROFILE;
    private boolean persistModuleStates = true;
    private Gson gson;
    
    private ConfigManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
        profilesPath = configPath.resolve(PROFILES_DIR);
    }
    
    public void init() {
        try {
            Files.createDirectories(configPath);
            Files.createDirectories(profilesPath);
            loadMainConfig();
            loadProfile(currentProfile);
        } catch (IOException e) {
            StartupLogger.logError("Failed to initialize config manager", e);
        }
    }
    
    /**
     * Save the main configuration file
     */
    public void saveMainConfig() {
        try {
            JsonObject config = new JsonObject();
            config.addProperty("currentProfile", currentProfile);
            config.addProperty("persistModuleStates", persistModuleStates);
            
            // Save available profiles list
            JsonArray profiles = new JsonArray();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesPath, "*.json")) {
                for (Path entry : stream) {
                    String name = entry.getFileName().toString().replace(".json", "");
                    profiles.add(name);
                }
            }
            config.add("profiles", profiles);
            
            Files.writeString(configPath.resolve(MAIN_CONFIG), gson.toJson(config));
        } catch (IOException e) {
            StartupLogger.logError("Failed to save main config", e);
        }
    }
    
    /**
     * Load the main configuration file
     */
    public void loadMainConfig() {
        Path mainConfigPath = configPath.resolve(MAIN_CONFIG);
        if (!Files.exists(mainConfigPath)) {
            saveMainConfig();
            return;
        }
        
        try {
            String content = Files.readString(mainConfigPath);
            JsonObject config = JsonParser.parseString(content).getAsJsonObject();
            
            if (config.has("currentProfile")) {
                currentProfile = config.get("currentProfile").getAsString();
            }
            if (config.has("persistModuleStates")) {
                persistModuleStates = config.get("persistModuleStates").getAsBoolean();
            }
        } catch (Exception e) {
            StartupLogger.logError("Failed to load main config", e);
        }
    }
    
    /**
     * Save the current profile
     */
    public void saveProfile(String profileName) {
        try {
            JsonObject profile = new JsonObject();
            JsonObject modules = new JsonObject();
            
            for (Module module : ModuleManager.INSTANCE.getModules()) {
                JsonObject moduleData = new JsonObject();
                moduleData.addProperty("enabled", module.isEnabled());
                moduleData.addProperty("key", module.getKey());
                
                // Save settings
                JsonObject settings = new JsonObject();
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting) {
                        settings.addProperty(setting.getName(), ((BooleanSetting) setting).isEnabled());
                    } else if (setting instanceof NumberSetting) {
                        settings.addProperty(setting.getName(), ((NumberSetting) setting).getValue());
                    } else if (setting instanceof ModeSetting) {
                        settings.addProperty(setting.getName(), ((ModeSetting) setting).getMode());
                    } else if (setting instanceof StringSetting) {
                        settings.addProperty(setting.getName(), ((StringSetting) setting).getValue());
                    } else if (setting instanceof ColorSetting) {
                        // Save color as JSON object with color value and rainbow state
                        JsonObject colorData = new JsonObject();
                        colorData.addProperty("color", ((ColorSetting) setting).getStaticColor());
                        colorData.addProperty("rainbow", ((ColorSetting) setting).isRainbow());
                        colorData.addProperty("rainbowSpeed", ((ColorSetting) setting).getRainbowSpeed());
                        settings.add(setting.getName(), colorData);
                    }
                }
                moduleData.add("settings", settings);
                
                modules.add(module.getName(), moduleData);
            }
            
            profile.add("modules", modules);
            
            Path profilePath = profilesPath.resolve(profileName + ".json");
            Files.writeString(profilePath, gson.toJson(profile));
            
            // Also update main config
            saveMainConfig();
        } catch (IOException e) {
            StartupLogger.logError("Failed to save profile: " + profileName, e);
        }
    }
    
    /**
     * Load a profile
     */
    public void loadProfile(String profileName) {
        Path profilePath = profilesPath.resolve(profileName + ".json");
        if (!Files.exists(profilePath)) {
            // Create default profile
            saveProfile(profileName);
            return;
        }
        
        try {
            String content = Files.readString(profilePath);
            JsonObject profile = JsonParser.parseString(content).getAsJsonObject();
            
            if (profile.has("modules")) {
                JsonObject modules = profile.getAsJsonObject("modules");
                
                for (Module module : ModuleManager.INSTANCE.getModules()) {
                    if (modules.has(module.getName())) {
                        JsonObject moduleData = modules.getAsJsonObject(module.getName());
                        
                        // Load enabled state only if persistence is enabled
                        if (persistModuleStates && moduleData.has("enabled")) {
                            boolean shouldBeEnabled = moduleData.get("enabled").getAsBoolean();
                            if (shouldBeEnabled != module.isEnabled()) {
                                module.toggle();
                            }
                        }
                        
                        // Load keybind
                        if (moduleData.has("key")) {
                            module.setKey(moduleData.get("key").getAsInt());
                        }
                        
                        // Load settings
                        if (moduleData.has("settings")) {
                            JsonObject settings = moduleData.getAsJsonObject("settings");
                            for (Setting setting : module.getSettings()) {
                                if (settings.has(setting.getName())) {
                                    JsonElement value = settings.get(setting.getName());
                                    if (setting instanceof BooleanSetting) {
                                        ((BooleanSetting) setting).setEnabled(value.getAsBoolean());
                                    } else if (setting instanceof NumberSetting) {
                                        ((NumberSetting) setting).setValue(value.getAsDouble());
                                    } else if (setting instanceof ModeSetting) {
                                        setModeValue((ModeSetting) setting, value.getAsString());
                                    } else if (setting instanceof StringSetting) {
                                        ((StringSetting) setting).setValue(value.getAsString());
                                    } else if (setting instanceof ColorSetting) {
                                        // Load color from JSON object
                                        if (value.isJsonObject()) {
                                            JsonObject colorData = value.getAsJsonObject();
                                            if (colorData.has("color")) {
                                                ((ColorSetting) setting).setColor(colorData.get("color").getAsInt());
                                            }
                                            if (colorData.has("rainbow")) {
                                                ((ColorSetting) setting).setRainbow(colorData.get("rainbow").getAsBoolean());
                                            }
                                            if (colorData.has("rainbowSpeed")) {
                                                ((ColorSetting) setting).setRainbowSpeed(colorData.get("rainbowSpeed").getAsFloat());
                                            }
                                        } else if (value.isJsonPrimitive()) {
                                            // Backwards compatibility: just color value
                                            ((ColorSetting) setting).setColor(value.getAsInt());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            currentProfile = profileName;
        } catch (Exception e) {
            StartupLogger.logError("Failed to load profile: " + profileName, e);
        }
    }
    
    private void setModeValue(ModeSetting setting, String mode) {
        // Cycle through modes until we find the right one, with max iteration limit
        String originalMode = setting.getMode();
        int maxIterations = 100; // Safety limit
        int iterations = 0;
        do {
            if (setting.getMode().equals(mode)) {
                return;
            }
            setting.cycle();
            iterations++;
            if (iterations >= maxIterations) {
                // Mode not found, stop trying
                return;
            }
        } while (!setting.getMode().equals(originalMode));
        // If we've cycled back to original without finding the mode, it doesn't exist
    }
    
    /**
     * Create a new profile
     */
    public void createProfile(String name) {
        saveProfile(name);
    }
    
    /**
     * Delete a profile
     */
    public void deleteProfile(String name) {
        if (name.equals(DEFAULT_PROFILE)) return; // Don't delete default
        
        try {
            Path profilePath = profilesPath.resolve(name + ".json");
            Files.deleteIfExists(profilePath);
            
            if (currentProfile.equals(name)) {
                loadProfile(DEFAULT_PROFILE);
            }
            saveMainConfig();
        } catch (IOException e) {
            StartupLogger.logError("Failed to delete profile: " + name, e);
        }
    }
    
    /**
     * Get list of available profiles
     */
    public List<String> getProfiles() {
        List<String> profiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesPath, "*.json")) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString().replace(".json", "");
                profiles.add(name);
            }
        } catch (IOException e) {
            StartupLogger.logError("Failed to list profiles", e);
        }
        
        if (profiles.isEmpty()) {
            profiles.add(DEFAULT_PROFILE);
        }
        
        return profiles;
    }
    
    public String getCurrentProfile() {
        return currentProfile;
    }
    
    public void setCurrentProfile(String profile) {
        this.currentProfile = profile;
        loadProfile(profile);
    }
    
    public boolean isPersistModuleStates() {
        return persistModuleStates;
    }
    
    public void setPersistModuleStates(boolean persist) {
        this.persistModuleStates = persist;
        saveMainConfig();
    }
    
    /**
     * Save current state - should be called periodically and on shutdown
     */
    public void save() {
        saveProfile(currentProfile);
        saveMainConfig();
    }
}
