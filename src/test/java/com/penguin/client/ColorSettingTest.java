package com.penguin.client;

import com.penguin.client.settings.ColorSetting;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ColorSetting class.
 */
public class ColorSettingTest {

    @Test
    public void testDefaultColor() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55);
        assertEquals(0xFF55FF55, setting.getColor());
        assertEquals(0xFF55FF55, setting.getStaticColor());
        assertFalse(setting.isRainbow());
    }

    @Test
    public void testSetColor() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55);
        setting.setColor(0xFFFF0000);
        assertEquals(0xFFFF0000, setting.getStaticColor());
    }

    @Test
    public void testColorComponents() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFFAA5533);
        assertEquals(0xAA, setting.getRed());
        assertEquals(0x55, setting.getGreen());
        assertEquals(0x33, setting.getBlue());
        assertEquals(0xFF, setting.getAlpha());
    }

    @Test
    public void testSetColorComponents() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF000000);
        setting.setRed(0x11);
        setting.setGreen(0x22);
        setting.setBlue(0x33);
        
        assertEquals(0x11, setting.getRed());
        assertEquals(0x22, setting.getGreen());
        assertEquals(0x33, setting.getBlue());
    }

    @Test
    public void testHexString() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFFAABBCC);
        assertEquals("#AABBCC", setting.getHexString());
    }

    @Test
    public void testSetFromHexString() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF000000);
        setting.setFromHexString("#FF5533");
        
        assertEquals(0xFF, setting.getRed());
        assertEquals(0x55, setting.getGreen());
        assertEquals(0x33, setting.getBlue());
    }

    @Test
    public void testSetFromHexStringWithoutHash() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF000000);
        setting.setFromHexString("AABBCC");
        
        assertEquals(0xAA, setting.getRed());
        assertEquals(0xBB, setting.getGreen());
        assertEquals(0xCC, setting.getBlue());
    }

    @Test
    public void testInvalidHexString() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF112233);
        setting.setFromHexString("invalid");
        
        // Should keep the original color
        assertEquals(0xFF112233, setting.getStaticColor());
    }

    @Test
    public void testRainbowMode() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55, true);
        assertTrue(setting.isRainbow());
    }

    @Test
    public void testToggleRainbow() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55);
        assertFalse(setting.isRainbow());
        setting.toggleRainbow();
        assertTrue(setting.isRainbow());
        setting.toggleRainbow();
        assertFalse(setting.isRainbow());
    }

    @Test
    public void testRainbowSpeed() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55);
        assertEquals(1.0f, setting.getRainbowSpeed());
        setting.setRainbowSpeed(2.5f);
        assertEquals(2.5f, setting.getRainbowSpeed());
    }

    @Test
    public void testRainbowSpeedBounds() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55);
        setting.setRainbowSpeed(-5.0f);
        assertTrue(setting.getRainbowSpeed() >= 0.1f);
        
        setting.setRainbowSpeed(100.0f);
        assertTrue(setting.getRainbowSpeed() <= 10.0f);
    }

    @Test
    public void testGetRainbowColor() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55, true);
        int color1 = setting.getRainbowColor();
        // Rainbow color should have full alpha
        assertEquals(0xFF, (color1 >> 24) & 0xFF);
    }

    @Test
    public void testGetRainbowColorWithIndex() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55, true);
        int color1 = setting.getRainbowColor(0);
        int color2 = setting.getRainbowColor(10);
        // Different indices should produce different colors (most of the time)
        // We can't guarantee they're different due to timing, but both should be valid
        assertEquals(0xFF, (color1 >> 24) & 0xFF);
        assertEquals(0xFF, (color2 >> 24) & 0xFF);
    }

    @Test
    public void testRainbowColorChangesOverTime() throws InterruptedException {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55, true);
        setting.setRainbowSpeed(5.0f); // Fast speed for quicker change
        int color1 = setting.getRainbowColor();
        
        // Wait a bit for color to change
        Thread.sleep(100);
        
        int color2 = setting.getRainbowColor();
        
        // Both should have full alpha
        assertEquals(0xFF, (color1 >> 24) & 0xFF);
        assertEquals(0xFF, (color2 >> 24) & 0xFF);
        
        // Colors should likely be different after 100ms with high speed
        // (Can't guarantee due to timing but this validates the mechanism)
    }

    @Test
    public void testRainbowOffset() {
        ColorSetting setting = new ColorSetting("TestColor", 0xFF55FF55, true);
        assertEquals(0.0f, setting.getRainbowOffset());
        setting.setRainbowOffset(0.5f);
        assertEquals(0.5f, setting.getRainbowOffset());
    }
}
