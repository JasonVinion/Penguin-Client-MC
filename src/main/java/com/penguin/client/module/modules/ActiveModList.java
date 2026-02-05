package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActiveModList extends Module {

    private BooleanSetting background = new BooleanSetting("Background", true);
    
    // Cache the enabled modules list to avoid expensive stream operations every frame
    private List<Module> cachedEnabledModules = new ArrayList<>();
    private int cacheUpdateCounter = 0;
    private static final int CACHE_UPDATE_INTERVAL = 3; // Update cache every 3 frames (~50ms at 60 FPS)

    public ActiveModList() {
        super("ActiveModList", "Displays a list of currently enabled modules on the screen HUD.", Category.RENDER);
        addSetting(background);
        // Default enabled
        if (!isEnabled()) toggle();
    }

    @Override
    public void onRender(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;

        // Only update the cached list periodically to reduce CPU usage
        cacheUpdateCounter++;
        if (cacheUpdateCounter > CACHE_UPDATE_INTERVAL || cachedEnabledModules.isEmpty()) {
            cacheUpdateCounter = 0;
            cachedEnabledModules.clear();
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                if (m.isEnabled() && !m.getName().equals("ActiveModList")) {
                    cachedEnabledModules.add(m);
                }
            }
            // Sort by width descending
            cachedEnabledModules.sort(Comparator.comparingInt(m -> -tr.getWidth(m.getName())));
        }
        
        List<Module> enabledModules = cachedEnabledModules;

        int screenWidth = mc.getWindow().getScaledWidth();
        int y = 10;
        int margin = 2;

        // Draw Title
        String title = "Active Mods";
        int titleWidth = tr.getWidth(title);
        int titleX = screenWidth - titleWidth - margin;
        int titleHeight = tr.fontHeight;

        if (background.isEnabled()) {
            context.fill(titleX - 2, y - 2, screenWidth, y + titleHeight + 2, 0x90000000);
        }
        
        // Use rainbow title color if available
        int titleColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getMenuTitleColor() : 0xFF5555FF;
        context.drawText(tr, Text.of(title), titleX, y, titleColor, true);
        y += titleHeight + margin + 2;

        int index = 0;
        for (Module module : enabledModules) {
            String text = module.getName();
            int width = tr.getWidth(text);
            int x = screenWidth - width - margin;
            int height = tr.fontHeight;

            if (background.isEnabled()) {
                context.fill(x - 2, y - 2, screenWidth, y + height + 2, 0x90000000);
            }

            // Use configurable active list color (supports rainbow per item)
            int color = ClientSettings.INSTANCE != null ? 
                ClientSettings.INSTANCE.getActiveListColor(index) : 0xFF55FF55;

            context.drawText(tr, Text.of(text), x, y, color, true);

            y += height + margin + 2;
            index++;
        }
    }
}
