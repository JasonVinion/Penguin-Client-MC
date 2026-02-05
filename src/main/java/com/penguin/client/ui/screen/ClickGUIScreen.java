package com.penguin.client.ui.screen;

import com.penguin.client.config.ConfigManager;
import com.penguin.client.config.KeybindConfig;
import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.ClientSettings;
import com.penguin.client.settings.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGUIScreen extends Screen {
    private static ClickGUIScreen INSTANCE;

    // Panel positions (persisted)
    private static Map<Category, int[]> panelPositions = new HashMap<>();
    private static Map<Category, Boolean> panelExpanded = new HashMap<>();

    private static final int PANEL_WIDTH = 140;
    private static final int PANEL_HEADER_HEIGHT = 22;
    private static final int MODULE_HEIGHT = 18;
    private static final int SETTING_HEIGHT = 14;
    private static final int MAX_MODULES = 12;
    private static final int MAX_SETTINGS = 10;
    private static final int ACCENT_BAR_WIDTH = 2;

    // State
    private static final int DRAG_THRESHOLD = 3;
    private Category draggingPanel = null;
    private Category pendingDragPanel = null;
    private int dragOffsetX, dragOffsetY;
    private int dragStartX, dragStartY;
    private Module expandedModule = null;
    private Map<Category, Integer> moduleScrollOffset = new HashMap<>();
    private int settingScrollOffset = 0;

    // Scroll bar dragging state
    private Category draggingScrollBar = null;
    private int scrollBarDragStartY = 0;
    private int scrollBarDragStartOffset = 0;

    // Cache for visible settings to avoid stream operations every frame
    private Module cachedSettingsModule = null;
    private List<Setting> cachedVisibleSettings = new ArrayList<>();
    private int cachedSettingsCount = -1;

    // Tooltip state
    private Module hoveredModule = null;

    // Search bar dimensions
    private static final int SEARCH_BAR_WIDTH = 150;
    private static final int SEARCH_BAR_HEIGHT = 22;
    private static int searchBarX = 10;
    private static int searchBarY = 10;

    // Search bar drag state
    private boolean draggingSearchBar = false;
    private boolean pendingSearchBarClick = false;
    private int searchBarDragOffsetX, searchBarDragOffsetY;
    private int searchBarClickStartX, searchBarClickStartY;

    private static final int ACCENT_COLOR       = 0xFF913DE2;
    // Backgrounds: dark grays with transparency
    private static final int BG_NORMAL          = 0xC8141414;
    private static final int BG_HOVERED         = 0xC81E1E1E;
    private static final int BG_PRESSED         = 0xC8282828;
    // Module-active background
    private static final int MODULE_ACTIVE_BG   = 0xFF323232;
    // Text
    private static final int TEXT_PRIMARY        = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY      = 0xFF969696;
    // Scrollbar
    private static final int SCROLLBAR_TRACK    = 0x40969696;
    private static final int SCROLLBAR_THUMB    = 0xAA969696;

    private int getAccentColor() {
        if (ClientSettings.INSTANCE != null) {
            return ClientSettings.INSTANCE.getMenuTitleColor();
        }
        return ACCENT_COLOR;
    }

    private int getBackgroundColor() {
        if (ClientSettings.INSTANCE != null) {
            return ClientSettings.INSTANCE.getMenuBackgroundColor();
        }
        return BG_NORMAL;
    }

    public ClickGUIScreen() {
        super(Text.of("Penguin Client"));
        INSTANCE = this;
    }

    public static ClickGUIScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUIScreen();
        }
        return INSTANCE;
    }

    private void initPanelPositions() {
        // Check if all non-SEARCH categories already have positions
        int requiredPositions = 0;
        for (Category cat : Category.values()) {
            if (cat != Category.SEARCH) requiredPositions++;
        }
        if (panelPositions.size() >= requiredPositions) {
            return; // All categories already have positions
        }
        
        // Initialize positions for any categories that don't have positions yet
        int x = 10;
        int y = 10;
        int spacing = PANEL_WIDTH + 10;
        
        // First, calculate starting position based on existing panels
        if (!panelPositions.isEmpty()) {
            // Find the next available position
            int maxX = 10;
            int maxY = 10;
            for (int[] pos : panelPositions.values()) {
                if (pos[1] > maxY || (pos[1] == maxY && pos[0] > maxX)) {
                    maxX = pos[0];
                    maxY = pos[1];
                }
            }
            x = maxX + spacing;
            y = maxY;
            if (x + PANEL_WIDTH > this.width) {
                x = 10;
                y += 200;
            }
        }

        for (Category category : Category.values()) {
            if (category == Category.SEARCH) continue; // Skip search for click GUI
            
            // Only add position if not already present
            if (!panelPositions.containsKey(category)) {
                panelPositions.put(category, new int[]{x, y});
                panelExpanded.put(category, true);
                x += spacing;
                if (x + PANEL_WIDTH > this.width) {
                    x = 10;
                    y += 200;
                }
            }
        }
    }

    @Override
    protected void init() {
        initPanelPositions();
        // Initialize scroll offsets
        for (Category category : Category.values()) {
            if (!moduleScrollOffset.containsKey(category)) {
                moduleScrollOffset.put(category, 0);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Semi-transparent dark overlay
        context.fill(0, 0, this.width, this.height, 0x88000000);

        // Reset hovered module for tooltip tracking
        hoveredModule = null;

        renderSearchBar(context, mouseX, mouseY);

        // Draw each category panel (only visible ones)
        for (Category category : Category.values()) {
            if (category == Category.SEARCH) continue;
            if (!category.isVisible()) continue;
            renderCategoryPanel(context, category, mouseX, mouseY);
        }

        // Draw search results panel if there's a search query
        if (!ModuleManager.searchQuery.isEmpty()) {
            renderSearchResultsPanel(context, mouseX, mouseY);
        }

        // Draw tooltip for hovered module
        if (hoveredModule != null && hoveredModule.getDescription() != null && !hoveredModule.getDescription().isEmpty()) {
            renderTooltip(context, mouseX, mouseY, hoveredModule.getDescription());
        }

        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Renders a tooltip near the mouse cursor.
     */
    private void renderTooltip(DrawContext context, int mouseX, int mouseY, String text) {
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - 4;
        int padding = 4;
        int textW = this.textRenderer.getWidth(text);
        int textH = this.textRenderer.fontHeight;

        // Keep tooltip on screen
        if (tooltipX + textW + padding * 2 > this.width) {
            tooltipX = mouseX - textW - padding * 2 - 4;
        }
        if (tooltipY + textH + padding * 2 > this.height) {
            tooltipY = mouseY - textH - padding * 2;
        }

        context.fill(tooltipX, tooltipY, tooltipX + textW + padding * 2, tooltipY + textH + padding * 2, BG_PRESSED);
        context.drawText(this.textRenderer, Text.of(text), tooltipX + padding, tooltipY + padding, TEXT_PRIMARY, false);
    }

    private void renderSearchBar(DrawContext context, int mouseX, int mouseY) {
        int x = searchBarX;
        int y = searchBarY;

        boolean hovered = mouseX >= x && mouseX <= x + SEARCH_BAR_WIDTH &&
                          mouseY >= y && mouseY <= y + SEARCH_BAR_HEIGHT;

        // Accent-colored header bar (same as category panels)
        context.fill(x, y, x + SEARCH_BAR_WIDTH, y + SEARCH_BAR_HEIGHT, getAccentColor());

        // Search icon + text
        String displayText = ModuleManager.searchQuery.isEmpty() ? "\u2315 Search..." : "\u2315 " + ModuleManager.searchQuery;
        int textColor = ModuleManager.searchQuery.isEmpty() ? 0xDDFFFFFF : TEXT_PRIMARY;

        int textY = y + (SEARCH_BAR_HEIGHT - this.textRenderer.fontHeight) / 2;
        context.drawText(this.textRenderer, Text.of(displayText),
            x + 6, textY, textColor, false);
    }

    private void renderSearchResultsPanel(DrawContext context, int mouseX, int mouseY) {
        List<Module> searchResults = ModuleManager.INSTANCE.getModulesByCategory(Category.SEARCH);
        if (searchResults.isEmpty()) return;

        int x = searchBarX;
        int y = searchBarY + SEARCH_BAR_HEIGHT;

        // Dark body background for results
        int visibleCount = Math.min(searchResults.size(), MAX_MODULES);
        // Pre-calculate total height including expanded settings
        int totalHeight = 0;
        for (int i = 0; i < visibleCount; i++) {
            totalHeight += MODULE_HEIGHT;
            Module module = searchResults.get(i);
            if (expandedModule == module) {
                List<Setting> settings = getVisibleSettings(module);
                int totalSettings = settings.size() + 2;
                totalHeight += Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
            }
        }
        context.fill(x, y, x + SEARCH_BAR_WIDTH, y + totalHeight, getBackgroundColor());

        // Draw results count label

        // Draw search results with inline settings
        int moduleY = y;
        for (int i = 0; i < visibleCount; i++) {
            Module module = searchResults.get(i);

            boolean hovered = mouseX >= x && mouseX <= x + SEARCH_BAR_WIDTH &&
                             mouseY >= moduleY && mouseY < moduleY + MODULE_HEIGHT;

            // Background: active = MODULE_ACTIVE_BG, hovered = BG_HOVERED, else transparent
            if (module.isEnabled()) {
                context.fill(x, moduleY, x + SEARCH_BAR_WIDTH, moduleY + MODULE_HEIGHT, MODULE_ACTIVE_BG);
            } else if (hovered) {
                context.fill(x, moduleY, x + SEARCH_BAR_WIDTH, moduleY + MODULE_HEIGHT, BG_HOVERED);
            }

            // Purple accent bar on left for active modules
            if (module.isEnabled()) {
                context.fill(x, moduleY, x + ACCENT_BAR_WIDTH, moduleY + MODULE_HEIGHT, getAccentColor());
            }

            // Module name — always white
            int textY = moduleY + (MODULE_HEIGHT - this.textRenderer.fontHeight) / 2;
            String displayName = module.getName();
            if (expandedModule == module) {
                displayName = "\u25BC " + displayName;
            } else if (!module.getSettings().isEmpty()) {
                displayName = "\u25B6 " + displayName;
            }
            context.drawText(this.textRenderer, Text.of(displayName),
                x + 6, textY, TEXT_PRIMARY, false);

            if (hovered) {
                hoveredModule = module;
            }

            moduleY += MODULE_HEIGHT;

            // Render inline settings if this module is expanded
            if (expandedModule == module) {
                moduleY = renderInlineSettingsForSearch(context, module, x, moduleY, mouseX, mouseY);
            }
        }

        // Show "more..." if there are more results
        if (searchResults.size() > MAX_MODULES) {
            context.drawText(this.textRenderer,
                Text.of("+" + (searchResults.size() - MAX_MODULES) + " more..."),
                x + 6, moduleY + 3, TEXT_SECONDARY, false);
        }
    }

    /**
     * Renders inline settings for search results (uses SEARCH_BAR_WIDTH instead of PANEL_WIDTH).
     */
    private int renderInlineSettingsForSearch(DrawContext context, Module module, int x, int startY, int mouseX, int mouseY) {
        List<Setting> settings = getVisibleSettings(module);
        int settingIndent = 10;
        int settingWidth = SEARCH_BAR_WIDTH - settingIndent;

        int settingY = startY;
        int totalSettings = settings.size() + 2;

        // Draw darker background for settings area
        int settingsHeight = Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
        context.fill(x + settingIndent, settingY, x + SEARCH_BAR_WIDTH, settingY + settingsHeight, BG_PRESSED);

        for (int j = settingScrollOffset; j < Math.min(totalSettings, settingScrollOffset + MAX_SETTINGS); j++) {
            boolean hovered = mouseX >= x + settingIndent && mouseX <= x + SEARCH_BAR_WIDTH &&
                             mouseY >= settingY && mouseY < settingY + SETTING_HEIGHT;
            int textColor = hovered ? TEXT_PRIMARY : TEXT_SECONDARY;

            String text;
            if (j == 0) {
                text = "Enabled: " + (module.isEnabled() ? "ON" : "OFF");
                textColor = module.isEnabled() ? getAccentColor() : TEXT_SECONDARY;
            } else if (j == 1) {
                String keyName = module.getKey() == -1 ? "None" :
                    com.penguin.client.config.KeybindConfig.getKeyName(module.getKey());
                text = "Bind: " + keyName;
            } else {
                Setting setting = settings.get(j - 2);
                text = formatSettingText(setting);
                if (setting instanceof BooleanSetting && ((BooleanSetting) setting).isEnabled()) {
                    textColor = hovered ? TEXT_PRIMARY : getAccentColor();
                }
            }

            // Truncate if too long
            while (this.textRenderer.getWidth(text) > settingWidth - 8 && text.length() > 5) {
                text = text.substring(0, text.length() - 4) + "...";
            }

            int textY = settingY + (SETTING_HEIGHT - this.textRenderer.fontHeight) / 2;
            context.drawText(this.textRenderer, Text.of(text),
                x + settingIndent + 4, textY, textColor, false);

            settingY += SETTING_HEIGHT;
        }

        return settingY;
    }

    private void renderCategoryPanel(DrawContext context, Category category, int mouseX, int mouseY) {
        int[] pos = panelPositions.get(category);
        int x = pos[0];
        int y = pos[1];
        boolean expanded = panelExpanded.getOrDefault(category, true);

        List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(category);

        // ── Header: solid accent color bar with white centered text ──
        context.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEADER_HEIGHT, getAccentColor());

        String headerText = category.getName();
        int headerTextW = this.textRenderer.getWidth(headerText);
        int headerTextX = x + (PANEL_WIDTH - headerTextW) / 2;
        int headerTextY = y + (PANEL_HEADER_HEIGHT - this.textRenderer.fontHeight) / 2;
        context.drawText(this.textRenderer, Text.of(headerText),
            headerTextX, headerTextY, TEXT_PRIMARY, false);

        // Expand/collapse triangle on right side
        String indicator = expanded ? "\u25BC" : "\u25B6";
        context.drawText(this.textRenderer, Text.of(indicator),
            x + PANEL_WIDTH - 14, headerTextY, TEXT_PRIMARY, false);

        if (!expanded) return;

        // ── Body: dark background ──
        int scrollOffset = moduleScrollOffset.getOrDefault(category, 0);

        int moduleY = y + PANEL_HEADER_HEIGHT;
        int bodyStartY = moduleY;
        int visibleModules = 0;

        // First pass: calculate body height for background
        int bodyHeight = 0;
        for (int i = scrollOffset; i < Math.min(modules.size(), scrollOffset + MAX_MODULES); i++) {
            bodyHeight += MODULE_HEIGHT;
            if (expandedModule == modules.get(i)) {
                List<Setting> settings = getVisibleSettings(modules.get(i));
                int totalSettings = settings.size() + 2;
                bodyHeight += Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
            }
        }
        context.fill(x, bodyStartY, x + PANEL_WIDTH, bodyStartY + bodyHeight, getBackgroundColor());

        // Draw modules
        for (int i = scrollOffset; i < Math.min(modules.size(), scrollOffset + MAX_MODULES); i++) {
            Module module = modules.get(i);

            boolean hovered = mouseX >= x && mouseX <= x + PANEL_WIDTH - 5 &&
                             mouseY >= moduleY && mouseY < moduleY + MODULE_HEIGHT;

            // Background: active = MODULE_ACTIVE_BG, hovered = BG_HOVERED
            if (module.isEnabled()) {
                context.fill(x, moduleY, x + PANEL_WIDTH - 3, moduleY + MODULE_HEIGHT, MODULE_ACTIVE_BG);
            } else if (hovered) {
                context.fill(x, moduleY, x + PANEL_WIDTH - 3, moduleY + MODULE_HEIGHT, BG_HOVERED);
            }

            if (module.isEnabled()) {
                context.fill(x, moduleY, x + ACCENT_BAR_WIDTH, moduleY + MODULE_HEIGHT, getAccentColor());
            }

            // Module name — always white text
            int textY = moduleY + (MODULE_HEIGHT - this.textRenderer.fontHeight) / 2;
            String displayName = module.getName();
            if (expandedModule == module) {
                displayName = "\u25BC " + displayName;
            } else if (!module.getSettings().isEmpty()) {
                displayName = "\u25B6 " + displayName;
            }
            context.drawText(this.textRenderer, Text.of(displayName),
                x + 6, textY, TEXT_PRIMARY, false);

            if (hovered) {
                hoveredModule = module;
            }

            moduleY += MODULE_HEIGHT;
            visibleModules++;

            // Render inline settings if this module is expanded
            if (expandedModule == module) {
                moduleY = renderInlineSettings(context, module, x, moduleY, mouseX, mouseY);
            }
        }

        // Draw scroll bar if needed
        if (modules.size() > MAX_MODULES) {
            int scrollBarHeight = MAX_MODULES * MODULE_HEIGHT;
            int thumbHeight = Math.max(20, scrollBarHeight * MAX_MODULES / modules.size());
            int maxOffset = modules.size() - MAX_MODULES;
            int thumbY = maxOffset > 0 ? y + PANEL_HEADER_HEIGHT + (scrollOffset * (scrollBarHeight - thumbHeight) / maxOffset)
                                       : y + PANEL_HEADER_HEIGHT;

            // Track
            context.fill(x + PANEL_WIDTH - 3, y + PANEL_HEADER_HEIGHT,
                        x + PANEL_WIDTH, y + PANEL_HEADER_HEIGHT + scrollBarHeight, SCROLLBAR_TRACK);
            // Thumb
            context.fill(x + PANEL_WIDTH - 3, thumbY,
                        x + PANEL_WIDTH, thumbY + thumbHeight, SCROLLBAR_THUMB);
        }
    }

    /**
     * Renders inline settings below a module in the same panel.
     * Returns the Y position after rendering all settings.
     */
    private int renderInlineSettings(DrawContext context, Module module, int x, int startY, int mouseX, int mouseY) {
        List<Setting> settings = getVisibleSettings(module);
        int settingIndent = 10;
        int settingWidth = PANEL_WIDTH - settingIndent - 3;

        int settingY = startY;
        int totalSettings = settings.size() + 2; // +2 for Enabled and Keybind

        // Draw darker background for settings area
        int settingsHeight = Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
        context.fill(x + settingIndent, settingY, x + PANEL_WIDTH - 3, settingY + settingsHeight, BG_PRESSED);

        for (int j = settingScrollOffset; j < Math.min(totalSettings, settingScrollOffset + MAX_SETTINGS); j++) {
            boolean hovered = mouseX >= x + settingIndent && mouseX <= x + PANEL_WIDTH - 3 &&
                             mouseY >= settingY && mouseY < settingY + SETTING_HEIGHT;
            int textColor = hovered ? TEXT_PRIMARY : TEXT_SECONDARY;

            String text;
            if (j == 0) {
                // Enabled toggle
                text = "Enabled: " + (module.isEnabled() ? "ON" : "OFF");
                textColor = module.isEnabled() ? getAccentColor() : TEXT_SECONDARY;
            } else if (j == 1) {
                // Keybind
                String keyName = module.getKey() == -1 ? "None" :
                    com.penguin.client.config.KeybindConfig.getKeyName(module.getKey());
                text = "Bind: " + keyName;
            } else {
                Setting setting = settings.get(j - 2);
                text = formatSettingText(setting);
                if (setting instanceof BooleanSetting && ((BooleanSetting) setting).isEnabled()) {
                    textColor = hovered ? TEXT_PRIMARY : getAccentColor();
                }
            }

            // Truncate if too long
            while (this.textRenderer.getWidth(text) > settingWidth - 8 && text.length() > 5) {
                text = text.substring(0, text.length() - 4) + "...";
            }

            int textY = settingY + (SETTING_HEIGHT - this.textRenderer.fontHeight) / 2;
            context.drawText(this.textRenderer, Text.of(text),
                x + settingIndent + 4, textY, textColor, false);

            settingY += SETTING_HEIGHT;
        }

        return settingY;
    }

    /**
     * Formats a setting's display text based on its type.
     */
    private String formatSettingText(Setting setting) {
        String text = setting.getName();
        if (setting instanceof BooleanSetting) {
            text += ": " + (((BooleanSetting) setting).isEnabled() ? "ON" : "OFF");
        } else if (setting instanceof NumberSetting) {
            double val = ((NumberSetting) setting).getValue();
            text += ": " + (val == (int)val ? String.valueOf((int)val) : String.format("%.2f", val));
        } else if (setting instanceof ModeSetting) {
            text += ": " + ((ModeSetting) setting).getMode();
        } else if (setting instanceof StringSetting) {
            String val = ((StringSetting) setting).getValue();
            if (val.length() > 8) val = val.substring(0, 8) + "...";
            text += ": " + val;
        } else if (setting instanceof ActionSetting) {
            text += " [RUN]";
        } else if (setting instanceof ColorSetting) {
            text += " [COLOR]";
        }
        return text;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check search bar click first - set up for potential drag or click
        if (mouseX >= searchBarX && mouseX <= searchBarX + SEARCH_BAR_WIDTH &&
            mouseY >= searchBarY && mouseY <= searchBarY + SEARCH_BAR_HEIGHT) {
            if (button == 0) {
                // Start potential drag
                pendingSearchBarClick = true;
                searchBarClickStartX = (int) mouseX;
                searchBarClickStartY = (int) mouseY;
                searchBarDragOffsetX = (int) mouseX - searchBarX;
                searchBarDragOffsetY = (int) mouseY - searchBarY;
                return true;
            }
        }

        // Check search results click
        if (!ModuleManager.searchQuery.isEmpty()) {
            List<Module> searchResults = ModuleManager.INSTANCE.getModulesByCategory(Category.SEARCH);
            if (!searchResults.isEmpty()) {
                int searchX = searchBarX;
                int searchY = searchBarY + SEARCH_BAR_HEIGHT;
                int settingIndent = 10;
                
                int visibleCount = Math.min(searchResults.size(), MAX_MODULES);
                for (int i = 0; i < visibleCount; i++) {
                    Module module = searchResults.get(i);
                    
                    // Check click on module name
                    if (mouseX >= searchX && mouseX <= searchX + SEARCH_BAR_WIDTH &&
                        mouseY >= searchY && mouseY < searchY + MODULE_HEIGHT) {
                        
                        if (button == 0) {
                            // Left click - toggle
                            module.toggle();
                        } else if (button == 1) {
                            // Right click - show/hide inline settings
                            if (expandedModule == module) {
                                expandedModule = null;
                                settingScrollOffset = 0;
                            } else {
                                expandedModule = module;
                                settingScrollOffset = 0;
                            }
                        }
                        return true;
                    }
                    searchY += MODULE_HEIGHT;
                    
                    // Check clicks on inline settings if this module is expanded
                    if (expandedModule == module) {
                        List<Setting> settings = getVisibleSettings(module);
                        int totalSettings = settings.size() + 2;
                        
                        for (int j = settingScrollOffset; j < Math.min(totalSettings, settingScrollOffset + MAX_SETTINGS); j++) {
                            if (mouseX >= searchX + settingIndent && mouseX <= searchX + SEARCH_BAR_WIDTH - 3 &&
                                mouseY >= searchY && mouseY < searchY + SETTING_HEIGHT) {
                                
                                if (j == 0) {
                                    // Toggle enabled
                                    module.toggle();
                                } else if (j == 1) {
                                    // Open keybind screen
                                    MinecraftClient.getInstance().setScreen(new KeybindScreen(this, module));
                                } else {
                                    Setting setting = settings.get(j - 2);
                                    handleSettingClick(setting, button);
                                }
                                return true;
                            }
                            searchY += SETTING_HEIGHT;
                        }
                    }
                }
            }
        }

        // Check category panels
        for (Category category : Category.values()) {
            if (category == Category.SEARCH) continue;
            if (!category.isVisible()) continue; // Skip hidden categories

            int[] pos = panelPositions.get(category);
            if (pos == null) continue;
            int x = pos[0];
            int y = pos[1];

            // Check header click (drag or expand/collapse)
            if (mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                mouseY >= y && mouseY <= y + PANEL_HEADER_HEIGHT) {

                // Check if clicking on the expand/collapse arrow (right side of header)
                if (mouseX >= x + PANEL_WIDTH - 15 && button == 0) {
                    // Left click on arrow - toggle expand
                    panelExpanded.put(category, !panelExpanded.getOrDefault(category, true));
                } else if (button == 0) {
                    // Left click elsewhere - toggle on release or drag if moved
                    pendingDragPanel = category;
                    dragStartX = (int) mouseX;
                    dragStartY = (int) mouseY;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - y;
                } else if (button == 1) {
                    // Right click - toggle expand
                    panelExpanded.put(category, !panelExpanded.getOrDefault(category, true));
                }
                return true;
            }

            // Check module clicks
            if (panelExpanded.getOrDefault(category, true)) {
                List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(category);
                int scrollOffset = moduleScrollOffset.getOrDefault(category, 0);
                int moduleY = y + PANEL_HEADER_HEIGHT;
                int settingIndent = 10;

                for (int i = scrollOffset; i < Math.min(modules.size(), scrollOffset + MAX_MODULES); i++) {
                    Module module = modules.get(i);
                    
                    // Check click on module name
                    if (mouseX >= x && mouseX <= x + PANEL_WIDTH - 5 &&
                        mouseY >= moduleY && mouseY < moduleY + MODULE_HEIGHT) {

                        if (button == 0) {
                            // Left click - toggle
                            module.toggle();
                        } else if (button == 1) {
                            // Right click - show/hide inline settings
                            if (expandedModule == module) {
                                expandedModule = null;
                                settingScrollOffset = 0;
                            } else {
                                expandedModule = module;
                                settingScrollOffset = 0;
                            }
                        }
                        return true;
                    }
                    moduleY += MODULE_HEIGHT;
                    
                    // Check clicks on inline settings if this module is expanded
                    if (expandedModule == module) {
                        List<Setting> settings = getVisibleSettings(module);
                        int totalSettings = settings.size() + 2;
                        
                        for (int j = settingScrollOffset; j < Math.min(totalSettings, settingScrollOffset + MAX_SETTINGS); j++) {
                            if (mouseX >= x + settingIndent && mouseX <= x + PANEL_WIDTH - 3 &&
                                mouseY >= moduleY && mouseY < moduleY + SETTING_HEIGHT) {
                                
                                if (j == 0) {
                                    // Toggle enabled
                                    module.toggle();
                                } else if (j == 1) {
                                    // Open keybind screen
                                    MinecraftClient.getInstance().setScreen(new KeybindScreen(this, module));
                                } else {
                                    Setting setting = settings.get(j - 2);
                                    handleSettingClick(setting, button);
                                }
                                return true;
                            }
                            moduleY += SETTING_HEIGHT;
                        }
                    }
                }
                
                // Check scroll bar click for this category
                if (modules.size() > MAX_MODULES && button == 0) {
                    int scrollBarX = x + PANEL_WIDTH - 3;
                    int scrollBarY = y + PANEL_HEADER_HEIGHT;
                    int scrollBarHeight = MAX_MODULES * MODULE_HEIGHT;
                    
                    if (mouseX >= scrollBarX && mouseX <= x + PANEL_WIDTH &&
                        mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {
                        // Start dragging scroll bar
                        draggingScrollBar = category;
                        scrollBarDragStartY = (int) mouseY;
                        scrollBarDragStartOffset = moduleScrollOffset.getOrDefault(category, 0);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSettingClick(Setting setting, int button) {
        if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).toggle();
        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            if (button == 0) {
                ns.increment();
            } else {
                ns.decrement();
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            if (button == 0) {
                ms.cycle();
            } else {
                ms.cycleBack();
            }
        } else if (setting instanceof StringSetting) {
            StringSetting ss = (StringSetting) setting;
            MinecraftClient.getInstance().setScreen(new StringEditorScreen(this, ss.getValue(), ss::setValue));
        } else if (setting instanceof ActionSetting) {
            ((ActionSetting) setting).execute();
        } else if (setting instanceof ColorSetting) {
            MinecraftClient.getInstance().setScreen(new ColorPickerScreen(this, (ColorSetting) setting));
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Handle search bar release - open text editor only if not dragged
        if (pendingSearchBarClick && !draggingSearchBar) {
            pendingSearchBarClick = false;
            MinecraftClient.getInstance().setScreen(new StringEditorScreen(this, ModuleManager.searchQuery, s -> {
                ModuleManager.searchQuery = s;
            }));
            return true;
        }
        if (draggingSearchBar) {
            draggingSearchBar = false;
            pendingSearchBarClick = false;
            return true;
        }
        
        if (draggingPanel != null) {
            draggingPanel = null;
            pendingDragPanel = null;
            return true;
        }
        if (pendingDragPanel != null) {
            panelExpanded.put(pendingDragPanel, !panelExpanded.getOrDefault(pendingDragPanel, true));
            pendingDragPanel = null;
            return true;
        }
        if (draggingScrollBar != null) {
            draggingScrollBar = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Handle search bar dragging
        if (pendingSearchBarClick && !draggingSearchBar) {
            int deltaXInt = (int) Math.abs(mouseX - searchBarClickStartX);
            int deltaYInt = (int) Math.abs(mouseY - searchBarClickStartY);
            if (deltaXInt > DRAG_THRESHOLD || deltaYInt > DRAG_THRESHOLD) {
                draggingSearchBar = true;
            }
        }
        if (draggingSearchBar) {
            searchBarX = Math.max(0, Math.min(this.width - SEARCH_BAR_WIDTH, (int) mouseX - searchBarDragOffsetX));
            searchBarY = Math.max(0, Math.min(this.height - SEARCH_BAR_HEIGHT, (int) mouseY - searchBarDragOffsetY));
            return true;
        }
        
        if (pendingDragPanel != null && draggingPanel == null) {
            int deltaXInt = (int) Math.abs(mouseX - dragStartX);
            int deltaYInt = (int) Math.abs(mouseY - dragStartY);
            if (deltaXInt > DRAG_THRESHOLD || deltaYInt > DRAG_THRESHOLD) {
                draggingPanel = pendingDragPanel;
            } else {
                return true;
            }
        }
        if (draggingPanel != null) {
            int[] pos = panelPositions.get(draggingPanel);
            pos[0] = Math.max(0, Math.min(this.width - PANEL_WIDTH, (int) mouseX - dragOffsetX));
            pos[1] = Math.max(0, Math.min(this.height - PANEL_HEADER_HEIGHT, (int) mouseY - dragOffsetY));
            return true;
        }
        
        // Handle scroll bar dragging
        if (draggingScrollBar != null) {
            List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(draggingScrollBar);
            if (modules.size() > MAX_MODULES) {
                int maxOffset = modules.size() - MAX_MODULES;
                int scrollBarHeight = MAX_MODULES * MODULE_HEIGHT;
                int thumbHeight = Math.max(20, scrollBarHeight * MAX_MODULES / modules.size());
                int dragRange = scrollBarHeight - thumbHeight;
                
                int deltaScroll = (int) mouseY - scrollBarDragStartY;
                int newOffset = scrollBarDragStartOffset + (deltaScroll * maxOffset / Math.max(1, dragRange));
                newOffset = Math.max(0, Math.min(maxOffset, newOffset));
                moduleScrollOffset.put(draggingScrollBar, newOffset);
            }
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // Check if scrolling in search results with inline settings
        if (expandedModule != null && !ModuleManager.searchQuery.isEmpty()) {
            List<Module> searchResults = ModuleManager.INSTANCE.getModulesByCategory(Category.SEARCH);
            if (searchResults.contains(expandedModule)) {
                // Calculate Y position of the expanded module's settings in search results
                int searchX = searchBarX;
                int searchY = searchBarY + SEARCH_BAR_HEIGHT;
                for (Module m : searchResults) {
                    if (searchResults.indexOf(m) >= MAX_MODULES) break;
                    searchY += MODULE_HEIGHT;
                    if (m == expandedModule) {
                        // Settings start here
                        List<Setting> settings = getVisibleSettings(expandedModule);
                        int totalSettings = settings.size() + 2;
                        int settingsHeight = Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
                        
                        // Check if mouse is within the settings area
                        if (mouseX >= searchX && mouseX <= searchX + SEARCH_BAR_WIDTH &&
                            mouseY >= searchY && mouseY <= searchY + settingsHeight) {
                            if (amount > 0 && settingScrollOffset > 0) {
                                settingScrollOffset--;
                                return true;
                            } else if (amount < 0 && settingScrollOffset + MAX_SETTINGS < totalSettings) {
                                settingScrollOffset++;
                                return true;
                            }
                        }
                        break;
                    }
                    // Account for inline settings if this module is expanded
                    if (expandedModule == m) {
                        List<Setting> settings = getVisibleSettings(m);
                        int totalSettings = settings.size() + 2;
                        searchY += Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
                    }
                }
            }
        }
        
        // Check if scrolling in inline settings (now rendered within category panels)
        if (expandedModule != null) {
            Category category = expandedModule.getCategory();
            int[] pos = panelPositions.get(category);
            if (pos != null) {
                int x = pos[0];
                int y = pos[1];
                int settingIndent = 10;

                // Calculate Y position of the expanded module's settings
                List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(category);
                int scrollOffset = moduleScrollOffset.getOrDefault(category, 0);
                int moduleY = y + PANEL_HEADER_HEIGHT;
                
                for (int i = scrollOffset; i < Math.min(modules.size(), scrollOffset + MAX_MODULES); i++) {
                    Module m = modules.get(i);
                    moduleY += MODULE_HEIGHT;
                    
                    if (m == expandedModule) {
                        // Settings start here
                        List<Setting> settings = getVisibleSettings(expandedModule);
                        int totalSettings = settings.size() + 2;
                        int settingsHeight = Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
                        
                        // Check if mouse is within the settings area
                        if (mouseX >= x + settingIndent && mouseX <= x + PANEL_WIDTH - 3 &&
                            mouseY >= moduleY && mouseY <= moduleY + settingsHeight) {
                            if (amount > 0 && settingScrollOffset > 0) {
                                settingScrollOffset--;
                                return true;
                            } else if (amount < 0 && settingScrollOffset + MAX_SETTINGS < totalSettings) {
                                settingScrollOffset++;
                                return true;
                            }
                        }
                        break;
                    }
                    
                    // Account for inline settings if this module is expanded
                    if (expandedModule == m) {
                        List<Setting> settings = getVisibleSettings(m);
                        int totalSettings = settings.size() + 2;
                        moduleY += Math.min(totalSettings, MAX_SETTINGS) * SETTING_HEIGHT;
                    }
                }
            }
        }

        // Check if scrolling in category panels
        for (Category category : Category.values()) {
            if (category == Category.SEARCH) continue;
            if (!category.isVisible()) continue; // Skip hidden categories
            if (!panelExpanded.getOrDefault(category, true)) continue;

            int[] pos = panelPositions.get(category);
            if (pos == null) continue;
            int x = pos[0];
            int y = pos[1];

            List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(category);
            if (modules.size() <= MAX_MODULES) continue;

            int panelHeight = PANEL_HEADER_HEIGHT + Math.min(modules.size(), MAX_MODULES) * MODULE_HEIGHT;

            if (mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                mouseY >= y && mouseY <= y + panelHeight) {

                int scrollOffset = moduleScrollOffset.getOrDefault(category, 0);
                if (amount > 0 && scrollOffset > 0) {
                    moduleScrollOffset.put(category, scrollOffset - 1);
                } else if (amount < 0 && scrollOffset + MAX_MODULES < modules.size()) {
                    moduleScrollOffset.put(category, scrollOffset + 1);
                }
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int toggleKey = KeybindConfig.INSTANCE.getMenuToggleKey();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || (toggleKey >= 0 && keyCode == toggleKey)) {
            ConfigManager.INSTANCE.save();
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        ConfigManager.INSTANCE.save();
        this.client.setScreen(null);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
    /**
     * Gets visible settings for a module, with caching to reduce allocations.
     * Cache is invalidated when module changes or total settings count changes.
     */
    private List<Setting> getVisibleSettings(Module module) {
        int totalSettingsCount = module.getSettings().size();
        if (cachedSettingsModule != module || cachedSettingsCount != totalSettingsCount) {
            cachedSettingsModule = module;
            cachedSettingsCount = totalSettingsCount;
            cachedVisibleSettings.clear();
            for (Setting s : module.getSettings()) {
                if (s.isVisible()) {
                    cachedVisibleSettings.add(s);
                }
            }
        }
        return cachedVisibleSettings;
    }
}
