package com.penguin.client;

import com.penguin.client.config.KeybindConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.lwjgl.glfw.GLFW;

/**
 * Tests for the KeybindConfig class.
 */
public class KeybindConfigTest {

    @Test
    public void testDefaultMenuToggleKey() {
        // Since INSTANCE is a singleton, we can test it, but it may have been modified
        // For unit testing, we test the getKeyName utility method instead
        String keyName = KeybindConfig.getKeyName(GLFW.GLFW_KEY_INSERT);
        assertEquals("INSERT", keyName);
    }

    @Test
    public void testGetKeyNameArrowKeys() {
        assertEquals("UP", KeybindConfig.getKeyName(GLFW.GLFW_KEY_UP));
        assertEquals("DOWN", KeybindConfig.getKeyName(GLFW.GLFW_KEY_DOWN));
        assertEquals("LEFT", KeybindConfig.getKeyName(GLFW.GLFW_KEY_LEFT));
        assertEquals("RIGHT", KeybindConfig.getKeyName(GLFW.GLFW_KEY_RIGHT));
    }

    @Test
    public void testGetKeyNameSpecialKeys() {
        assertEquals("ENTER", KeybindConfig.getKeyName(GLFW.GLFW_KEY_ENTER));
        assertEquals("BACKSPACE", KeybindConfig.getKeyName(GLFW.GLFW_KEY_BACKSPACE));
        assertEquals("SPACE", KeybindConfig.getKeyName(GLFW.GLFW_KEY_SPACE));
        assertEquals("TAB", KeybindConfig.getKeyName(GLFW.GLFW_KEY_TAB));
        assertEquals("ESC", KeybindConfig.getKeyName(GLFW.GLFW_KEY_ESCAPE));
    }

    @Test
    public void testGetKeyNameFunctionKeys() {
        assertEquals("F1", KeybindConfig.getKeyName(GLFW.GLFW_KEY_F1));
        assertEquals("F5", KeybindConfig.getKeyName(GLFW.GLFW_KEY_F5));
        assertEquals("F12", KeybindConfig.getKeyName(GLFW.GLFW_KEY_F12));
    }

    @Test
    public void testGetKeyNameModifierKeys() {
        assertEquals("L_SHIFT", KeybindConfig.getKeyName(GLFW.GLFW_KEY_LEFT_SHIFT));
        assertEquals("R_SHIFT", KeybindConfig.getKeyName(GLFW.GLFW_KEY_RIGHT_SHIFT));
        assertEquals("L_CTRL", KeybindConfig.getKeyName(GLFW.GLFW_KEY_LEFT_CONTROL));
        assertEquals("R_CTRL", KeybindConfig.getKeyName(GLFW.GLFW_KEY_RIGHT_CONTROL));
        assertEquals("L_ALT", KeybindConfig.getKeyName(GLFW.GLFW_KEY_LEFT_ALT));
        assertEquals("R_ALT", KeybindConfig.getKeyName(GLFW.GLFW_KEY_RIGHT_ALT));
    }

    @Test
    public void testGetKeyNameNone() {
        assertEquals("None", KeybindConfig.getKeyName(-1));
    }

    @Test
    public void testGetKeyNameNumpad() {
        assertEquals("NUM_0", KeybindConfig.getKeyName(GLFW.GLFW_KEY_KP_0));
        assertEquals("NUM_5", KeybindConfig.getKeyName(GLFW.GLFW_KEY_KP_5));
        assertEquals("NUM_9", KeybindConfig.getKeyName(GLFW.GLFW_KEY_KP_9));
    }

    @Test
    public void testGetKeyNameEditingKeys() {
        assertEquals("DELETE", KeybindConfig.getKeyName(GLFW.GLFW_KEY_DELETE));
        assertEquals("HOME", KeybindConfig.getKeyName(GLFW.GLFW_KEY_HOME));
        assertEquals("END", KeybindConfig.getKeyName(GLFW.GLFW_KEY_END));
        assertEquals("PAGE_UP", KeybindConfig.getKeyName(GLFW.GLFW_KEY_PAGE_UP));
        assertEquals("PAGE_DOWN", KeybindConfig.getKeyName(GLFW.GLFW_KEY_PAGE_DOWN));
    }

    @Test
    public void testGetKeyNameUnknownKey() {
        // Unknown key should return KEY_<code>
        String keyName = KeybindConfig.getKeyName(9999);
        assertTrue(keyName.startsWith("KEY_"));
    }
}
