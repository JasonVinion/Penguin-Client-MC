package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;

/**
 * PortalGui - Allows opening GUIs like inventory and chat while in a portal
 */
public class PortalGui extends Module { // deprecated
    public static PortalGui INSTANCE;

    public PortalGui() {
        super("PortalGui", "Allows opening GUIs like inventory and chat while in a portal.", Category.PLAYER);
        INSTANCE = this;
        setVisible(false);
    }
    
    public static boolean shouldAllowGui() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
}
