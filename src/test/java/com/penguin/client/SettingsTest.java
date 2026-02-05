package com.penguin.client;

import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.StringSetting;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the various Setting classes.
 */
public class SettingsTest {

    // NumberSetting Tests
    @Test
    public void testNumberSettingDefaults() {
        NumberSetting setting = new NumberSetting("Test", 5.0, 0.0, 10.0, 1.0);
        assertEquals(5.0, setting.getValue());
        assertEquals("Test", setting.getName());
    }

    @Test
    public void testNumberSettingIncrement() {
        NumberSetting setting = new NumberSetting("Test", 5.0, 0.0, 10.0, 1.0);
        setting.increment();
        assertEquals(6.0, setting.getValue());
    }

    @Test
    public void testNumberSettingDecrement() {
        NumberSetting setting = new NumberSetting("Test", 5.0, 0.0, 10.0, 1.0);
        setting.decrement();
        assertEquals(4.0, setting.getValue());
    }

    @Test
    public void testNumberSettingMaxBound() {
        NumberSetting setting = new NumberSetting("Test", 10.0, 0.0, 10.0, 1.0);
        setting.increment();
        assertEquals(10.0, setting.getValue());
    }

    @Test
    public void testNumberSettingMinBound() {
        NumberSetting setting = new NumberSetting("Test", 0.0, 0.0, 10.0, 1.0);
        setting.decrement();
        assertEquals(0.0, setting.getValue());
    }

    @Test
    public void testNumberSettingStep() {
        NumberSetting setting = new NumberSetting("Test", 5.0, 0.0, 10.0, 0.5);
        setting.increment();
        assertEquals(5.5, setting.getValue());
    }

    // BooleanSetting Tests
    @Test
    public void testBooleanSettingDefaultTrue() {
        BooleanSetting setting = new BooleanSetting("Test", true);
        assertTrue(setting.isEnabled());
    }

    @Test
    public void testBooleanSettingDefaultFalse() {
        BooleanSetting setting = new BooleanSetting("Test", false);
        assertFalse(setting.isEnabled());
    }

    @Test
    public void testBooleanSettingToggle() {
        BooleanSetting setting = new BooleanSetting("Test", false);
        setting.toggle();
        assertTrue(setting.isEnabled());
        setting.toggle();
        assertFalse(setting.isEnabled());
    }

    @Test
    public void testBooleanSettingSetEnabled() {
        BooleanSetting setting = new BooleanSetting("Test", false);
        setting.setEnabled(true);
        assertTrue(setting.isEnabled());
        setting.setEnabled(false);
        assertFalse(setting.isEnabled());
    }

    // ModeSetting Tests
    @Test
    public void testModeSettingDefault() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        assertEquals("A", setting.getMode());
    }

    @Test
    public void testModeSettingCycle() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        setting.cycle();
        assertEquals("B", setting.getMode());
        setting.cycle();
        assertEquals("C", setting.getMode());
        setting.cycle();
        assertEquals("A", setting.getMode()); // Should wrap around
    }

    @Test
    public void testModeSettingCycleBack() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        setting.cycleBack();
        assertEquals("C", setting.getMode()); // Should wrap to end
        setting.cycleBack();
        assertEquals("B", setting.getMode());
    }

    @Test
    public void testModeSettingIs() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        assertTrue(setting.is("A"));
        assertFalse(setting.is("B"));
    }

    @Test
    public void testModeSettingGetModes() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        String[] modes = setting.getModes();
        assertEquals(3, modes.length);
        assertEquals("A", modes[0]);
        assertEquals("B", modes[1]);
        assertEquals("C", modes[2]);
    }
    
    @Test
    public void testModeSettingSetModeValid() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        setting.setMode("B");
        assertEquals("B", setting.getMode());
        setting.setMode("C");
        assertEquals("C", setting.getMode());
    }
    
    @Test
    public void testModeSettingSetModeInvalid() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        setting.setMode("D"); // Invalid mode
        assertEquals("A", setting.getMode()); // Should not change
    }
    
    @Test
    public void testModeSettingGetModesDefensiveCopy() {
        ModeSetting setting = new ModeSetting("Test", "A", "A", "B", "C");
        String[] modes = setting.getModes();
        modes[0] = "MODIFIED"; // Modify the returned array
        assertEquals("A", setting.getMode()); // Original should be unchanged
        assertEquals("A", setting.getModes()[0]); // Getting modes again should return original
    }

    // StringSetting Tests
    @Test
    public void testStringSettingDefault() {
        StringSetting setting = new StringSetting("Test", "Hello");
        assertEquals("Hello", setting.getValue());
    }

    @Test
    public void testStringSettingSetValue() {
        StringSetting setting = new StringSetting("Test", "Hello");
        setting.setValue("World");
        assertEquals("World", setting.getValue());
    }

    @Test
    public void testStringSettingEmptyDefault() {
        StringSetting setting = new StringSetting("Test", "");
        assertEquals("", setting.getValue());
    }

    // Setting Visibility Tests
    @Test
    public void testSettingVisibility() {
        BooleanSetting setting = new BooleanSetting("Test", true);
        assertTrue(setting.isVisible()); // Default should be visible
        setting.setVisible(false);
        assertFalse(setting.isVisible());
        setting.setVisible(true);
        assertTrue(setting.isVisible());
    }

    @Test
    public void testSettingName() {
        NumberSetting setting = new NumberSetting("My Setting Name", 5.0, 0.0, 10.0, 1.0);
        assertEquals("My Setting Name", setting.getName());
    }
}
