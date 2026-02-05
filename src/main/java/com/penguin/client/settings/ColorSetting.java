package com.penguin.client.settings;

/**
 * A setting that stores a color value with optional RGB (rainbow) mode.
 * Color is stored as an ARGB integer.
 */
public class ColorSetting extends Setting {
    private int color;
    private boolean rainbow;
    private float rainbowSpeed;
    private float rainbowOffset;

    public ColorSetting(String name, int defaultColor) {
        super(name);
        this.color = defaultColor;
        this.rainbow = false;
        this.rainbowSpeed = 1.0f;
        this.rainbowOffset = 0.0f;
    }

    public ColorSetting(String name, int defaultColor, boolean rainbow) {
        super(name);
        this.color = defaultColor;
        this.rainbow = rainbow;
        this.rainbowSpeed = 1.0f;
        this.rainbowOffset = 0.0f;
    }

    public int getColor() {
        if (rainbow) {
            return getRainbowColor();
        }
        return color;
    }

    public int getStaticColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public void toggleRainbow() {
        this.rainbow = !this.rainbow;
    }

    public float getRainbowSpeed() {
        return rainbowSpeed;
    }

    public void setRainbowSpeed(float rainbowSpeed) {
        this.rainbowSpeed = Math.max(0.1f, Math.min(10.0f, rainbowSpeed));
    }

    public float getRainbowOffset() {
        return rainbowOffset;
    }

    public void setRainbowOffset(float rainbowOffset) {
        this.rainbowOffset = rainbowOffset;
    }

    /**
     * Get the current rainbow color based on time.
     * The hue cycles through the full spectrum based on rainbowSpeed.
     * At speed 1.0, it completes one full cycle every ~1 second.
     */
    public int getRainbowColor() {
        // Use modulo on time first to keep numbers small, then scale
        long time = System.currentTimeMillis() % 3600000L; // Reset every hour to avoid overflow
        float hue = ((time * rainbowSpeed / 1000.0f) + rainbowOffset) % 1.0f;
        if (hue < 0) hue += 1.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | 0xFF000000;
    }

    /**
     * Get a rainbow color with an offset for each item in a list.
     * Each item has a slight hue offset to create a wave effect.
     */
    public int getRainbowColor(int itemIndex) {
        long time = System.currentTimeMillis() % 3600000L; // Reset every hour to avoid overflow
        float hue = ((time * rainbowSpeed / 1000.0f) + rainbowOffset + itemIndex * 0.05f) % 1.0f;
        if (hue < 0) hue += 1.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | 0xFF000000;
    }

    // Helper methods for getting color components
    public int getRed() {
        return (color >> 16) & 0xFF;
    }

    public int getGreen() {
        return (color >> 8) & 0xFF;
    }

    public int getBlue() {
        return color & 0xFF;
    }

    public int getAlpha() {
        return (color >> 24) & 0xFF;
    }

    public void setRed(int red) {
        this.color = (color & 0xFF00FFFF) | ((red & 0xFF) << 16);
    }

    public void setGreen(int green) {
        this.color = (color & 0xFFFF00FF) | ((green & 0xFF) << 8);
    }

    public void setBlue(int blue) {
        this.color = (color & 0xFFFFFF00) | (blue & 0xFF);
    }

    public void setAlpha(int alpha) {
        this.color = (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /**
     * Get hex string representation of the color.
     * Returns #RRGGBB if alpha is 255, otherwise #AARRGGBB.
     */
    public String getHexString() {
        int alpha = (color >> 24) & 0xFF;
        if (alpha == 255) {
            return String.format("#%06X", (color & 0xFFFFFF));
        } else {
            return String.format("#%08X", color);
        }
    }

    /**
     * Set color from hex string (e.g., "#FF5555" or "#AAFF5555").
     */
    public void setFromHexString(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if (hex.length() == 6) {
                int rgb = Integer.parseInt(hex, 16);
                this.color = rgb | 0xFF000000;
            } else if (hex.length() == 8) {
                long argb = Long.parseLong(hex, 16);
                this.color = (int) argb;
            }
        } catch (NumberFormatException e) {
            // Invalid hex, ignore
        }
    }
}
