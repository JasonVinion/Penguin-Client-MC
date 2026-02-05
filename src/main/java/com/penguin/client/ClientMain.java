package com.penguin.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import com.penguin.client.config.ConfigManager;
import com.penguin.client.config.KeybindConfig;
import com.penguin.client.logging.StartupLogger;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.ui.InputHandler;
import com.penguin.client.ui.MenuHUD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientMain implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("penguin-client");
    private static int saveTimer = 0;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Penguin Client Initializing...");
        StartupLogger.init("penguin-client-startup.log");
        StartupLogger.log("Penguin Client Initializing...");
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Uncaught exception on thread {}", thread.getName(), throwable);
            StartupLogger.logError("Uncaught exception on thread " + thread.getName(), throwable);
        });
        ModuleManager.INSTANCE.init();
        StartupLogger.log("Module initialization completed.");
        
        // Initialize keybind config
        KeybindConfig.INSTANCE.init();
        StartupLogger.log("Keybind config initialized.");
        
        // Initialize config manager and load saved settings
        ConfigManager.INSTANCE.init();
        StartupLogger.log("Config manager initialized.");

        // Register HUD
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MenuHUD.render(context, tickDelta);
            ModuleManager.INSTANCE.onRender(context);
        });

        // Register Input and Tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            InputHandler.update();
            ModuleManager.INSTANCE.onTick();
            
            // Auto-save every 5 minutes (6000 ticks)
            saveTimer++;
            if (saveTimer >= 6000) {
                saveTimer = 0;
                ConfigManager.INSTANCE.save();
            }
        });
        
        // Register shutdown hook to save config
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            StartupLogger.log("Saving config on shutdown...");
            ConfigManager.INSTANCE.save();
        }));
    }
}
