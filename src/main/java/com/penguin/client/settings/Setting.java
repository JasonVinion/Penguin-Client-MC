package com.penguin.client.settings;

public abstract class Setting {
    protected String name;
    protected boolean visible;

    public Setting(String name) {
        this.name = name;
        this.visible = true;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
