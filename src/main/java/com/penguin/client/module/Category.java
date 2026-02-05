package com.penguin.client.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc"),
    SETTINGS("Settings"),
    TESTING("Testing"),
    SEARCH("Search");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    /**
     * Returns true if this category should be visible in the UI.
     * The TESTING category is only visible when beta tester mode is enabled.
     */
    public boolean isVisible() {
        if (this == TESTING) {
            // Check if beta tester mode is enabled via BetaTesterMode module
            try {
                com.penguin.client.module.modules.BetaTesterMode betaMode = 
                    com.penguin.client.module.modules.BetaTesterMode.INSTANCE;
                return betaMode != null && betaMode.isBetaTesterEnabled();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }
}
