package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ModeSetting;
import net.minecraft.client.MinecraftClient;

/**
 * FancyChat - Makes your chat text look fancy/gamer
 */
public class FancyChat extends Module {
    public static FancyChat INSTANCE;
    
    private ModeSetting style = new ModeSetting("Style", "Small Caps", "Small Caps", "Bubble", "Squares");

    public FancyChat() {
        super("FancyChat", "Makes your chat text look fancy/gamer with Unicode.", Category.MISC);
        addSetting(style);
        INSTANCE = this;
    }
    
    public static String transformText(String text) {
        if (INSTANCE == null || !INSTANCE.isEnabled()) {
            return text;
        }
        
        String styleMode = INSTANCE.style.getMode();
        StringBuilder result = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            switch (styleMode) {
                case "Small Caps":
                    result.append(toSmallCaps(c));
                    break;
                case "Bubble":
                    result.append(toBubble(c));
                    break;
                case "Squares":
                    result.append(toSquares(c));
                    break;
                default:
                    result.append(c);
            }
        }
        
        return result.toString();
    }
    
    private static char toSmallCaps(char c) {
        // Small caps mapping - these are scattered in Unicode, so we need a lookup table
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String smallCaps = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ";
        int index = lowercase.indexOf(c);
        if (index >= 0) {
            return smallCaps.charAt(index);
        }
        return c;
    }
    
    private static char toBubble(char c) {
        if (c >= 'a' && c <= 'z') {
            return (char) (0x24D0 + (c - 'a'));
        } else if (c >= 'A' && c <= 'Z') {
            return (char) (0x24B6 + (c - 'A'));
        } else if (c >= '0' && c <= '9') {
            // Map 0->⓪ (0x24EA), 1->① (0x2460), 2->② (0x2461), etc.
            if (c == '0') return (char) 0x24EA;
            return (char) (0x2460 + (c - '1'));
        }
        return c;
    }
    
    private static char toSquares(char c) {
        if (c >= 'A' && c <= 'Z') {
            return (char) (0x1F130 + (c - 'A'));
        } else if (c >= 'a' && c <= 'z') {
            return (char) (0x1F130 + (c - 'a'));
        }
        return c;
    }
}
