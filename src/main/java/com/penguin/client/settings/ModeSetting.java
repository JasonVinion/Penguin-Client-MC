package com.penguin.client.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    private int index;
    private List<String> modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        if (this.index == -1) this.index = 0;
    }

    public String getMode() {
        return modes.get(index);
    }
    
    public String[] getModes() {
        return modes.toArray(new String[0]);
    }

    public boolean is(String mode) {
        return index >= 0 && index < modes.size() && modes.get(index).equalsIgnoreCase(mode);
    }

    public void cycle() {
        if (index < modes.size() - 1) {
            index++;
        } else {
            index = 0;
        }
    }

    public void cycleBack() {
        if (index > 0) {
            index--;
        } else {
            index = modes.size() - 1;
        }
    }
    
    public void setMode(String mode) {
        int newIndex = modes.indexOf(mode);
        if (newIndex != -1) {
            this.index = newIndex;
        }
    }
}
