package com.penguin.client.settings;

public class ActionSetting extends Setting {
    private final Runnable action;

    public ActionSetting(String name, Runnable action) {
        super(name);
        this.action = action;
    }

    public void execute() {
        if (action != null) {
            action.run();
        }
    }
}
