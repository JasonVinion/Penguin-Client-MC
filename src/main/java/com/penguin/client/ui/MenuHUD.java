package com.penguin.client.ui;

import com.penguin.client.config.ConfigManager;
import com.penguin.client.config.KeybindConfig;
import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.ClientSettings;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.ColorSetting;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.settings.Setting;
import com.penguin.client.settings.StringSetting;
import com.penguin.client.ui.screen.KeybindScreen;
import com.penguin.client.ui.screen.StringEditorScreen;
import com.penguin.client.ui.screen.ColorPickerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuHUD {
    private static boolean visible = false;
    private static int currentCategoryIndex = 0;
    private static int currentModuleIndex = 0;
    private static int currentSettingIndex = 0;

    private static int moduleOffset = 0;
    private static int settingOffset = 0;
    private static final int MAX_ITEMS = 7;

    private static boolean expanded = false;
    private static boolean inSettings = false;
    
    // Track collapsed state for each category
    private static java.util.Map<Category, Boolean> categoryCollapsed = new java.util.HashMap<>();

    /**
     * Gets a list of visible categories based on current settings.
     * The TESTING category is only visible when beta tester mode is enabled.
     */
    private static List<Category> getVisibleCategories() {
        List<Category> visible = new ArrayList<>();
        for (Category cat : Category.values()) {
            if (cat.isVisible()) {
                visible.add(cat);
            }
        }
        return visible;
    }

    public static void openCategory(Category category) {
        List<Category> visibleCategories = getVisibleCategories();
        int index = visibleCategories.indexOf(category);
        if (index != -1) {
            currentCategoryIndex = index;
            expanded = true;
            currentModuleIndex = 0;
            moduleOffset = 0;
        }
    }

    public static void toggleVisibility() {
        visible = !visible;
    }

    public static boolean isVisible() {
        return visible;
    }
    
    public static void setVisible(boolean state) {
        visible = state;
    }

    public static void handleInput(int key) {
        if (!visible) return;

        KeybindConfig config = KeybindConfig.INSTANCE;
        int upKey = config.getNavigateUpKey();
        int downKey = config.getNavigateDownKey();
        int leftKey = config.getNavigateLeftKey();
        int rightKey = config.getNavigateRightKey();
        int selectKey = config.getSelectKey();
        int backKey = config.getBackKey();

        List<Category> visibleCategories = getVisibleCategories();
        
        // Handle empty categories case
        if (visibleCategories.isEmpty()) {
            if (key == backKey) {
                visible = false;
                ConfigManager.INSTANCE.save();
            }
            return;
        }
        
        // Clamp category index to visible categories
        if (currentCategoryIndex >= visibleCategories.size()) {
            currentCategoryIndex = 0;
        }
        if (currentCategoryIndex < 0) {
            currentCategoryIndex = 0;
        }

        if (!expanded) {
            // Category Navigation
            if (key == backKey) {
                visible = false;
                ConfigManager.INSTANCE.save(); // Save on close
                return;
            }
            if (key == downKey) {
                currentCategoryIndex++;
                if (currentCategoryIndex >= visibleCategories.size()) currentCategoryIndex = 0;
            } else if (key == upKey) {
                currentCategoryIndex--;
                if (currentCategoryIndex < 0) currentCategoryIndex = visibleCategories.size() - 1;
            } else if (key == leftKey) {
                // Left key toggles collapse on current category
                Category currentCategory = visibleCategories.get(currentCategoryIndex);
                boolean isCollapsed = categoryCollapsed.getOrDefault(currentCategory, false);
                categoryCollapsed.put(currentCategory, !isCollapsed);
            } else if (key == rightKey || key == selectKey) {
                Category currentCategory = visibleCategories.get(currentCategoryIndex);
                
                // Uncollapse if collapsed
                if (categoryCollapsed.getOrDefault(currentCategory, false)) {
                    categoryCollapsed.put(currentCategory, false);
                    return;
                }

                if (currentCategory == Category.SEARCH) {
                    if (key == selectKey) {
                        MinecraftClient.getInstance().setScreen(new StringEditorScreen(null, ModuleManager.searchQuery, s -> {
                            ModuleManager.searchQuery = s;
                            if (!ModuleManager.INSTANCE.getModulesByCategory(Category.SEARCH).isEmpty()) {
                                MenuHUD.openCategory(Category.SEARCH);
                            }
                        }));
                        return;
                    }
                }

                if (!ModuleManager.INSTANCE.getModulesByCategory(currentCategory).isEmpty()) {
                    expanded = true;
                    currentModuleIndex = 0;
                    moduleOffset = 0;
                } else if (currentCategory == Category.SEARCH) {
                     MinecraftClient.getInstance().setScreen(new StringEditorScreen(null, ModuleManager.searchQuery, s -> {
                         ModuleManager.searchQuery = s;
                         if (!ModuleManager.INSTANCE.getModulesByCategory(Category.SEARCH).isEmpty()) {
                             MenuHUD.openCategory(Category.SEARCH);
                         }
                     }));
                }
            }
        } else if (!inSettings) {
            // Module Navigation
            Category currentCategory = visibleCategories.get(currentCategoryIndex);
            List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(currentCategory);

            if (modules.isEmpty()) {
                 if (key == leftKey || key == backKey) {
                    expanded = false;
                 }
                 return;
            }

            if (key == downKey) {
                currentModuleIndex++;
                if (currentModuleIndex >= modules.size()) currentModuleIndex = 0;

                if (currentModuleIndex < moduleOffset) moduleOffset = currentModuleIndex;
                else if (currentModuleIndex >= moduleOffset + MAX_ITEMS) moduleOffset = currentModuleIndex - MAX_ITEMS + 1;
                // Handle wrap to beginning
                if (currentModuleIndex == 0) moduleOffset = 0;
            } else if (key == upKey) {
                currentModuleIndex--;
                if (currentModuleIndex < 0) currentModuleIndex = modules.size() - 1;

                if (currentModuleIndex < moduleOffset) moduleOffset = currentModuleIndex;
                else if (currentModuleIndex >= moduleOffset + MAX_ITEMS) moduleOffset = currentModuleIndex - MAX_ITEMS + 1;
                // Handle wrap around on UP
                if (currentModuleIndex == modules.size() - 1) {
                    moduleOffset = Math.max(0, modules.size() - MAX_ITEMS);
                }
            } else if (key == leftKey || key == backKey) {
                expanded = false;
            } else if (key == rightKey || key == selectKey) {
                Module currentModule = modules.get(currentModuleIndex);
                inSettings = true;
                currentSettingIndex = 0;
                settingOffset = 0;
            }
        } else {
            // Settings Navigation
            Category currentCategory = visibleCategories.get(currentCategoryIndex);
            List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(currentCategory);
            Module currentModule = modules.get(currentModuleIndex);
            List<Setting> settings = getVisibleSettings(currentModule);

            // Settings: 0 = Enabled, 1 = Keybind, 2+ = actual settings
            int totalSettings = settings.size() + 2;

            if (key == downKey) {
                currentSettingIndex++;
                if (currentSettingIndex >= totalSettings) currentSettingIndex = 0;

                if (currentSettingIndex < settingOffset) settingOffset = currentSettingIndex;
                else if (currentSettingIndex >= settingOffset + MAX_ITEMS) settingOffset = currentSettingIndex - MAX_ITEMS + 1;
                if (currentSettingIndex == 0) settingOffset = 0;
            } else if (key == upKey) {
                currentSettingIndex--;
                if (currentSettingIndex < 0) currentSettingIndex = totalSettings - 1;

                if (currentSettingIndex < settingOffset) settingOffset = currentSettingIndex;
                else if (currentSettingIndex >= settingOffset + MAX_ITEMS) settingOffset = currentSettingIndex - MAX_ITEMS + 1;

                if (currentSettingIndex == totalSettings - 1) {
                    settingOffset = Math.max(0, totalSettings - MAX_ITEMS);
                }
            } else if (key == leftKey || key == backKey) {
                if (key == backKey) {
                    inSettings = false;
                    return;
                }

                if (currentSettingIndex == 0 || currentSettingIndex == 1) {
                    // Enabled or Keybind - go back
                    if (key == leftKey) inSettings = false;
                } else {
                    Setting setting = settings.get(currentSettingIndex - 2);
                    if (setting instanceof BooleanSetting) {
                         ((BooleanSetting) setting).setEnabled(false);
                    } else if (setting instanceof NumberSetting) {
                        ((NumberSetting) setting).decrement();
                    } else if (setting instanceof ModeSetting) {
                        ((ModeSetting) setting).cycleBack();
                    } else if (setting instanceof StringSetting) {
                        inSettings = false;
                    } else if (setting instanceof ActionSetting) {
                        // Do nothing on left/backspace for ActionSetting
                    }
                }
            } else if (key == rightKey || key == selectKey) {
                if (currentSettingIndex == 0) {
                    currentModule.toggle();
                } else if (currentSettingIndex == 1) {
                    // Open keybind screen
                    MinecraftClient.getInstance().setScreen(new KeybindScreen(null, currentModule));
                } else {
                    Setting setting = settings.get(currentSettingIndex - 2);
                    if (setting instanceof BooleanSetting) {
                        ((BooleanSetting) setting).toggle();
                    } else if (setting instanceof NumberSetting) {
                        ((NumberSetting) setting).increment();
                    } else if (setting instanceof ModeSetting) {
                        ((ModeSetting) setting).cycle();
                    } else if (setting instanceof StringSetting) {
                        MinecraftClient.getInstance().setScreen(new StringEditorScreen(null, ((StringSetting) setting).getValue(), s -> ((StringSetting) setting).setValue(s)));
                    } else if (setting instanceof ActionSetting) {
                        ((ActionSetting) setting).execute();
                    } else if (setting instanceof ColorSetting) {
                        MinecraftClient.getInstance().setScreen(new ColorPickerScreen(null, (ColorSetting) setting));
                    }
                }
            }
        }
    }

    public static void render(DrawContext context, float tickDelta) {
        if (!visible) return;

        int x = 20;
        int y = 20;
        int width = 100;
        int height = 14;
        int titleHeight = 18;

        // Get colors from ClientSettings if available
        int bgColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getMenuBackgroundColor() : 0xDD1a1a1a;
        int titleColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getMenuTitleColor() : 0xFF55FFFF;
        int selectedColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getSelectedItemColor() : 0x90000000;
        int unselectedColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getUnselectedItemColor() : 0x90222222;
        int highlightTextColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getHighlightedTextColor() : 0xFFFFFFFF;
        int normalTextColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getNormalTextColor() : 0xFFAAAAAA;

        // Draw title bar
        context.fill(x, y, x + width, y + titleHeight, bgColor);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Penguin Client"), x + 5, y + 5, titleColor, false);
        y += titleHeight + 2;

        List<Category> visibleCategories = getVisibleCategories();
        
        // Handle empty categories case
        if (visibleCategories.isEmpty()) {
            context.fill(x, y, x + width, y + height, unselectedColor);
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("No categories"), x + 5, y + 3, normalTextColor, false);
            return;
        }
        
        // Clamp category index
        if (currentCategoryIndex >= visibleCategories.size()) {
            currentCategoryIndex = 0;
        }
        if (currentCategoryIndex < 0) {
            currentCategoryIndex = 0;
        }

        int startY = y; // Capture start Y for column wrapping
        int originalY = y; // Keep track of original Y for renderModules alignment if needed

        int i = 0;
        for (Category category : visibleCategories) {
            // Check if we need to wrap to next column
            if (y + height > context.getScaledWindowHeight()) {
                y = startY;
                x += width + 2;
            }

            boolean selected = (i == currentCategoryIndex);
            int color = selected ? selectedColor : unselectedColor;

            context.fill(x, y, x + width, y + height, color);

            int textColor = selected ? highlightTextColor : normalTextColor;
            
            // Draw category name (no prefix indicator in list mode)
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(category.getName()), x + 5, y + 3, textColor, false);

            // Check if category is collapsed (used for module expansion)
            boolean isCollapsed = categoryCollapsed.getOrDefault(category, false);
            if (selected && expanded && !isCollapsed) {
                renderModules(context, category, x + width + 2, 20 + titleHeight + 2);
            }

            y += height;
            i++;
        }
    }

    private static void renderModules(DrawContext context, Category category, int x, int y) {
        List<Module> modules = ModuleManager.INSTANCE.getModulesByCategory(category);
        int width = 110;
        int height = 14;
        int titleHeight = 18;

        // Get colors from ClientSettings if available
        int bgColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getMenuBackgroundColor() : 0xDD1a1a1a;
        int titleColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getMenuTitleColor() : 0xFF55FFFF;
        int selectedColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getSelectedItemColor() : 0x90000000;
        int unselectedColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getUnselectedItemColor() : 0x90222222;
        int enabledColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getEnabledTextColor() : 0xFF55FF55;
        int highlightTextColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getHighlightedTextColor() : 0xFFFFFFFF;
        int normalTextColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getNormalTextColor() : 0xFFAAAAAA;

        // Draw header
        context.fill(x, y, x + width, y + titleHeight, bgColor);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(category.getName()), x + 5, y + 5, titleColor, false);
        y += titleHeight + 2;

        int startY = y;
        int displayCount = Math.min(modules.size() - moduleOffset, MAX_ITEMS);

        // Draw scroll indicator if needed (top)
        if (moduleOffset > 0) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("^"), x + width - 10, y - 10, 0xFFFFFF55, false);
        }

        for (int i = moduleOffset; i < Math.min(modules.size(), moduleOffset + MAX_ITEMS); i++) {
             Module module = modules.get(i);
             boolean selected = (i == currentModuleIndex);
             int color = selected ? selectedColor : unselectedColor;

             context.fill(x, y, x + width, y + height, color);

             int textColor = module.isEnabled() ? enabledColor : (selected ? highlightTextColor : normalTextColor);
             context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(module.getName()), x + 5, y + 3, textColor, false);

             if (selected && inSettings) {
                 renderSettings(context, module, x + width + 2, startY - titleHeight - 2);
             }

             y += height;
        }

        // Draw scroll indicator if needed (bottom)
        if (moduleOffset + MAX_ITEMS < modules.size()) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("v"), x + width - 10, y + 2, 0xFFFFFF55, false);
        }

        // Draw scroll bar
        if (modules.size() > MAX_ITEMS) {
            int scrollBarHeight = (MAX_ITEMS * height);
            int totalHeight = modules.size() * height;
            int thumbHeight = Math.max(20, (scrollBarHeight * scrollBarHeight) / totalHeight);
            int divisor = modules.size() - MAX_ITEMS;
            int thumbY = divisor > 0 ? startY + (moduleOffset * (scrollBarHeight - thumbHeight)) / divisor : startY;
            
            context.fill(x + width - 3, startY, x + width - 1, startY + scrollBarHeight, 0x40FFFFFF);
            context.fill(x + width - 3, thumbY, x + width - 1, thumbY + thumbHeight, 0xAAFFFFFF);
        }
    }

    private static void renderSettings(DrawContext context, Module module, int x, int y) {
        // Use cached visible settings list instead of filtering every frame
        List<Setting> settings = getVisibleSettings(module);
        int width = 130;
        int height = 14;
        int titleHeight = 18;

        // Get colors from ClientSettings if available
        int bgColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getMenuBackgroundColor() : 0xDD1a1a1a;
        int titleColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getMenuTitleColor() : 0xFF55FFFF;
        int selectedColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getSelectedItemColor() : 0x90000000;
        int unselectedColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getUnselectedItemColor() : 0x90222222;
        int enabledColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getEnabledTextColor() : 0xFF55FF55;
        int disabledColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getDisabledTextColor() : 0xFFFF5555;
        int highlightTextColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getHighlightedTextColor() : 0xFFFFFFFF;
        int normalTextColor = ClientSettings.INSTANCE != null ? ClientSettings.INSTANCE.getNormalTextColor() : 0xFFAAAAAA;

        // Draw header with module name
        context.fill(x, y, x + width, y + titleHeight, bgColor);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(module.getName()), x + 5, y + 5, titleColor, false);
        y += titleHeight + 2;

        int startY = y;
        int totalSettings = settings.size() + 2; // +2 for Enabled and Keybind

        // Draw scroll indicator if needed (top)
        if (settingOffset > 0) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("^"), x + width - 10, y - 10, 0xFFFFFF55, false);
        }

        for (int j = settingOffset; j < Math.min(totalSettings, settingOffset + MAX_ITEMS); j++) {
            boolean selected = (j == currentSettingIndex);
            int color = selected ? selectedColor : unselectedColor;

            context.fill(x, y, x + width, y + height, color);

            String text;
            int textColor = selected ? highlightTextColor : normalTextColor;

            if (j == 0) {
                 // Enabled Toggle
                 String status = module.isEnabled() ? "Enabled: ON" : "Enabled: OFF";
                 textColor = module.isEnabled() ? enabledColor : disabledColor;
                 if (selected) textColor = highlightTextColor;
                 text = status;
            } else if (j == 1) {
                // Keybind
                String keyName = KeybindConfig.getKeyName(module.getKey());
                text = "Keybind: " + keyName;
                textColor = module.getKey() != -1 ? titleColor : (selected ? highlightTextColor : normalTextColor);
            } else {
                 Setting setting = settings.get(j - 2);
                 text = setting.getName();
                 if (setting instanceof BooleanSetting) {
                     text += ": " + (((BooleanSetting) setting).isEnabled() ? "ON" : "OFF");
                     if (((BooleanSetting) setting).isEnabled()) {
                         if (!selected) textColor = enabledColor;
                     }
                 } else if (setting instanceof NumberSetting) {
                     text += ": " + String.format("%.2f", ((NumberSetting) setting).getValue());
                 } else if (setting instanceof ModeSetting) {
                     text += ": " + ((ModeSetting) setting).getMode();
                 } else if (setting instanceof StringSetting) {
                     String val = ((StringSetting) setting).getValue();
                     if (val.length() > 8) val = val.substring(0, 8) + "...";
                     text += ": " + val;
                 } else if (setting instanceof ActionSetting) {
                     text += " [CLICK]";
                 } else if (setting instanceof ColorSetting) {
                     text += " [COLOR]";
                 }
            }

            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(text), x + 5, y + 3, textColor, false);
            y += height;
        }

        // Draw scroll indicator if needed (bottom)
        if (settingOffset + MAX_ITEMS < totalSettings) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("v"), x + width - 10, y + 2, 0xFFFFFF55, false);
        }

        // Draw scroll bar
        if (totalSettings > MAX_ITEMS) {
            int scrollBarHeight = (MAX_ITEMS * height);
            int totalHeight = totalSettings * height;
            int thumbHeight = Math.max(20, (scrollBarHeight * scrollBarHeight) / totalHeight);
            int divisor = totalSettings - MAX_ITEMS;
            int thumbY = divisor > 0 ? startY + (settingOffset * (scrollBarHeight - thumbHeight)) / divisor : startY;
            
            context.fill(x + width - 3, startY, x + width - 1, startY + scrollBarHeight, 0x40FFFFFF);
            context.fill(x + width - 3, thumbY, x + width - 1, thumbY + thumbHeight, 0xAAFFFFFF);
        }

        // Draw module description tooltip at bottom - with word wrap for full text display
        if (module.getDescription() != null && !module.getDescription().isEmpty()) {
            String desc = module.getDescription();
            int maxWidth = width - 6;
            List<String> lines = wrapText(desc, maxWidth);
            
            int descY = startY + (MAX_ITEMS * height) + 5;
            int descHeight = lines.size() * 10 + 4;
            
            context.fill(x, descY, x + width, descY + descHeight, 0x90333333);
            
            for (int i = 0; i < lines.size(); i++) {
                context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(lines.get(i)), 
                    x + 3, descY + 2 + i * 10, 0xFFCCCCCC, false);
            }
        }
    }

    /**
     * Wraps text to fit within a maximum pixel width.
     */
    private static List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        net.minecraft.client.font.TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        for (String word : words) {
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (tr.getWidth(testLine) <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is too long, just add it
                    lines.add(word);
                }
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
    
    // Cache for visible settings to avoid stream operations every frame
    private static Module cachedSettingsModule = null;
    private static List<Setting> cachedVisibleSettings = new ArrayList<>();
    private static int cachedSettingsCount = -1;
    
    /**
     * Gets visible settings for a module, with caching to reduce allocations.
     * Cache is invalidated when module changes or total settings count changes.
     */
    private static List<Setting> getVisibleSettings(Module module) {
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
