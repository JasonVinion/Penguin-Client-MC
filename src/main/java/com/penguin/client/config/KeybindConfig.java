package com.penguin.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.penguin.client.logging.StartupLogger;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages keybind configuration for mod navigation keys.
 * Allows users to customize the menu toggle key and navigation keys.
 */
public class KeybindConfig {
    public static final KeybindConfig INSTANCE = new KeybindConfig();

    private static final String CONFIG_FILE = "keybinds.json";
    private Path configPath;

    // Default keybinds
    private int menuToggleKey = GLFW.GLFW_KEY_INSERT;
    private int navigateUpKey = GLFW.GLFW_KEY_UP;
    private int navigateDownKey = GLFW.GLFW_KEY_DOWN;
    private int navigateLeftKey = GLFW.GLFW_KEY_LEFT;
    private int navigateRightKey = GLFW.GLFW_KEY_RIGHT;
    private int selectKey = GLFW.GLFW_KEY_ENTER;
    private int backKey = GLFW.GLFW_KEY_BACKSPACE;

    private KeybindConfig() {
        try {
            if (FabricLoader.getInstance() != null) {
                configPath = FabricLoader.getInstance().getConfigDir().resolve("penguin-client");
            } else {
                configPath = Path.of("config/penguin-client");
            }
        } catch (Throwable t) {
            // Fallback for environments where FabricLoader is not available or throws error
            configPath = Path.of("config/penguin-client");
        }
    }

    public void init() {
        try {
            Files.createDirectories(configPath);
            load();
        } catch (IOException e) {
            StartupLogger.logError("Failed to initialize keybind config", e);
        }
    }

    public void save() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("menuToggleKey", menuToggleKey);
            json.addProperty("navigateUpKey", navigateUpKey);
            json.addProperty("navigateDownKey", navigateDownKey);
            json.addProperty("navigateLeftKey", navigateLeftKey);
            json.addProperty("navigateRightKey", navigateRightKey);
            json.addProperty("selectKey", selectKey);
            json.addProperty("backKey", backKey);

            Files.writeString(configPath.resolve(CONFIG_FILE), json.toString());
        } catch (IOException e) {
            StartupLogger.logError("Failed to save keybind config", e);
        }
    }

    public void load() {
        Path file = configPath.resolve(CONFIG_FILE);
        if (!Files.exists(file)) {
            save(); // Create default config
            return;
        }

        try {
            String content = Files.readString(file);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();

            if (json.has("menuToggleKey")) menuToggleKey = json.get("menuToggleKey").getAsInt();
            if (json.has("navigateUpKey")) navigateUpKey = json.get("navigateUpKey").getAsInt();
            if (json.has("navigateDownKey")) navigateDownKey = json.get("navigateDownKey").getAsInt();
            if (json.has("navigateLeftKey")) navigateLeftKey = json.get("navigateLeftKey").getAsInt();
            if (json.has("navigateRightKey")) navigateRightKey = json.get("navigateRightKey").getAsInt();
            if (json.has("selectKey")) selectKey = json.get("selectKey").getAsInt();
            if (json.has("backKey")) backKey = json.get("backKey").getAsInt();
        } catch (Exception e) {
            StartupLogger.logError("Failed to load keybind config", e);
            // Reset to defaults to ensure consistent state
            resetToDefaults();
        }
    }

    // Getters
    public int getMenuToggleKey() {
        return menuToggleKey;
    }

    public int getNavigateUpKey() {
        return navigateUpKey;
    }

    public int getNavigateDownKey() {
        return navigateDownKey;
    }

    public int getNavigateLeftKey() {
        return navigateLeftKey;
    }

    public int getNavigateRightKey() {
        return navigateRightKey;
    }

    public int getSelectKey() {
        return selectKey;
    }

    public int getBackKey() {
        return backKey;
    }

    // Setters
    public void setMenuToggleKey(int key) {
        this.menuToggleKey = key;
        save();
    }

    public void setNavigateUpKey(int key) {
        this.navigateUpKey = key;
        save();
    }

    public void setNavigateDownKey(int key) {
        this.navigateDownKey = key;
        save();
    }

    public void setNavigateLeftKey(int key) {
        this.navigateLeftKey = key;
        save();
    }

    public void setNavigateRightKey(int key) {
        this.navigateRightKey = key;
        save();
    }

    public void setSelectKey(int key) {
        this.selectKey = key;
        save();
    }

    public void setBackKey(int key) {
        this.backKey = key;
        save();
    }

    /**
     * Reset all keybinds to defaults.
     */
    public void resetToDefaults() {
        menuToggleKey = GLFW.GLFW_KEY_INSERT;
        navigateUpKey = GLFW.GLFW_KEY_UP;
        navigateDownKey = GLFW.GLFW_KEY_DOWN;
        navigateLeftKey = GLFW.GLFW_KEY_LEFT;
        navigateRightKey = GLFW.GLFW_KEY_RIGHT;
        selectKey = GLFW.GLFW_KEY_ENTER;
        backKey = GLFW.GLFW_KEY_BACKSPACE;
        save();
    }

    /**
     * Get a user-friendly key name.
     */
    public static String getKeyName(int keyCode) {
        if (keyCode == -1) return "None";

        String name = GLFW.glfwGetKeyName(keyCode, 0);
        if (name != null) {
            return name.toUpperCase();
        }

        // Handle special keys
        switch (keyCode) {
            case GLFW.GLFW_KEY_INSERT: return "INSERT";
            case GLFW.GLFW_KEY_DELETE: return "DELETE";
            case GLFW.GLFW_KEY_HOME: return "HOME";
            case GLFW.GLFW_KEY_END: return "END";
            case GLFW.GLFW_KEY_PAGE_UP: return "PAGE_UP";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PAGE_DOWN";
            case GLFW.GLFW_KEY_UP: return "UP";
            case GLFW.GLFW_KEY_DOWN: return "DOWN";
            case GLFW.GLFW_KEY_LEFT: return "LEFT";
            case GLFW.GLFW_KEY_RIGHT: return "RIGHT";
            case GLFW.GLFW_KEY_ENTER: return "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE: return "BACKSPACE";
            case GLFW.GLFW_KEY_SPACE: return "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "L_SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "R_SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "L_CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "R_CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT: return "L_ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "R_ALT";
            case GLFW.GLFW_KEY_TAB: return "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "CAPS";
            case GLFW.GLFW_KEY_ESCAPE: return "ESC";
            case GLFW.GLFW_KEY_F1: return "F1";
            case GLFW.GLFW_KEY_F2: return "F2";
            case GLFW.GLFW_KEY_F3: return "F3";
            case GLFW.GLFW_KEY_F4: return "F4";
            case GLFW.GLFW_KEY_F5: return "F5";
            case GLFW.GLFW_KEY_F6: return "F6";
            case GLFW.GLFW_KEY_F7: return "F7";
            case GLFW.GLFW_KEY_F8: return "F8";
            case GLFW.GLFW_KEY_F9: return "F9";
            case GLFW.GLFW_KEY_F10: return "F10";
            case GLFW.GLFW_KEY_F11: return "F11";
            case GLFW.GLFW_KEY_F12: return "F12";
            case GLFW.GLFW_KEY_KP_0: return "NUM_0";
            case GLFW.GLFW_KEY_KP_1: return "NUM_1";
            case GLFW.GLFW_KEY_KP_2: return "NUM_2";
            case GLFW.GLFW_KEY_KP_3: return "NUM_3";
            case GLFW.GLFW_KEY_KP_4: return "NUM_4";
            case GLFW.GLFW_KEY_KP_5: return "NUM_5";
            case GLFW.GLFW_KEY_KP_6: return "NUM_6";
            case GLFW.GLFW_KEY_KP_7: return "NUM_7";
            case GLFW.GLFW_KEY_KP_8: return "NUM_8";
            case GLFW.GLFW_KEY_KP_9: return "NUM_9";
            default: return "KEY_" + keyCode;
        }
    }
}
