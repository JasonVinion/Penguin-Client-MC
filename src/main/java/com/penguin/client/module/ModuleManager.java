package com.penguin.client.module;

import com.penguin.client.logging.StartupLogger;
import com.penguin.client.module.modules.*; // Import all modules
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private List<Module> modules = new ArrayList<>();
    private java.util.Map<Class<?>, Module> moduleCache = new java.util.HashMap<>();
    private java.util.Map<Category, List<Module>> categoryCache = new java.util.HashMap<>();

    private ModuleManager() {
    }

    public void init() {
        // Original Modules
        safeAddModule("ActiveModList", ActiveModList::new);
        safeAddModule("AntiEffect", AntiEffect::new);
        safeAddModule("AutoArmor", AutoArmor::new);
        safeAddModule("NoFall", NoFall::new);
        safeAddModule("AntiWaterPush", AntiWaterPush::new);
        safeAddModule("FastPlace", FastPlace::new);
        safeAddModule("FullBright", FullBright::new);
        safeAddModule("XRay", XRay::new);
        safeAddModule("Flight", Flight::new);
        safeAddModule("Sprint", Sprint::new);
        safeAddModule("Step", Step::new);
        safeAddModule("Jesus", Jesus::new);
        safeAddModule("Velocity", Velocity::new);

        // Self
        safeAddModule("AutoRespawn", AutoRespawn::new);
        safeAddModule("AutoTool", AutoTool::new);
        safeAddModule("AutoEat", AutoEat::new);
        safeAddModule("FastExp", FastExp::new);
        safeAddModule("AutoFish", AutoFish::new);
        safeAddModule("AutoMount", AutoMount::new);
        safeAddModule("AntiVoid", AntiVoid::new);
        safeAddModule("AntiHunger", AntiHunger::new);

        // Utility
        safeAddModule("Spammer", Spammer::new);
        safeAddModule("ChatSuffix", ChatSuffix::new);
        safeAddModule("Panic", Panic::new);
        safeAddModule("AutoDisconnect", AutoDisconnect::new);
        safeAddModule("InvCleaner", InvCleaner::new);

        // Render
        safeAddModule("ESP", ESP::new);
        safeAddModule("Tracers", Tracers::new);
        safeAddModule("NameTags", NameTags::new);
        safeAddModule("NoWeather", NoWeather::new);
        safeAddModule("Zoom", Zoom::new);
        safeAddModule("BlockESP", BlockESP::new);
        safeAddModule("ItemESP", ItemESP::new);
        safeAddModule("PotionSpoofer", PotionSpoofer::new);

        // Movement
        safeAddModule("AutoWalk", AutoWalk::new);
        safeAddModule("AutoJump", AutoJump::new);
        safeAddModule("HighJump", HighJump::new);
        safeAddModule("Spider", Spider::new);
        safeAddModule("FastSwim", FastSwim::new);
        safeAddModule("AirJump", AirJump::new);
        safeAddModule("NoSlow", NoSlow::new);
        safeAddModule("Parkour", Parkour::new);

        // Combat
        safeAddModule("KillAura", KillAura::new);
        safeAddModule("AutoTotem", AutoTotem::new);
        safeAddModule("TriggerBot", TriggerBot::new);
        safeAddModule("AutoSoup", AutoSoup::new);
        safeAddModule("Criticals", Criticals::new);
        safeAddModule("BowAim", BowAim::new);
        safeAddModule("Reach", Reach::new);
        safeAddModule("HitBox", HitBox::new);
        safeAddModule("AutoWeapon", AutoWeapon::new);
        safeAddModule("AimAssist", AimAssist::new);
        safeAddModule("AntiAFK", AntiAFK::new);
        safeAddModule("FeedAura", FeedAura::new);
        safeAddModule("InfiniteChat", InfiniteChat::new);
        safeAddModule("FastMine", FastMine::new);
        safeAddModule("AutoFarm", AutoFarm::new);
        safeAddModule("Scaffold", Scaffold::new);
        safeAddModule("Speed", Speed::new);
        safeAddModule("Freecam", Freecam::new);
        safeAddModule("AntiUnderwaterFog", AntiUnderwaterFog::new);
        safeAddModule("SafeWalk", SafeWalk::new);
        safeAddModule("AntiEntityPush", AntiEntityPush::new);
        safeAddModule("PowderSnowWalk", PowderSnowWalk::new);
        safeAddModule("Strafe", Strafe::new);
        safeAddModule("StrafeAura", StrafeAura::new);
        safeAddModule("AutoCrystal", AutoCrystal::new);
        safeAddModule("AutoPot", AutoPot::new);
        safeAddModule("SprintReset", SprintReset::new);
        // NoHitDelay removed per issue request
        safeAddModule("NoJumpDelay", NoJumpDelay::new);
        safeAddModule("LongJump", LongJump::new);
        safeAddModule("TridentFly", TridentFly::new);
        safeAddModule("Sneak", Sneak::new);
        safeAddModule("HotbarRefill", Replenish::new);
        safeAddModule("AntiLevitation", AntiLevitation::new);
        safeAddModule("XCarry", XCarry::new);
        safeAddModule("Trajectories", Trajectories::new);
        // AutoMine removed per issue request
        safeAddModule("AutoTunnel", AutoTunnel::new);

        // New Modules
        safeAddModule("AutoWeb", AutoWeb::new);
        safeAddModule("FastBow", FastBow::new);
        safeAddModule("BoatFly", BoatFly::new);
        safeAddModule("InventoryMove", InventoryMove::new);
        safeAddModule("Phase", Phase::new);
        safeAddModule("ChestESP", ChestESP::new);
        // FastUse removed per issue request
        safeAddModule("LiquidInteract", LiquidInteract::new);
        safeAddModule("Nuker", Nuker::new);
        safeAddModule("Timer", Timer::new);
        safeAddModule("ObsidianCover", ObsidianCover::new);
        safeAddModule("EntityControl", EntityControl::new);

        // New combat modules
        safeAddModule("HoleESP", HoleESP::new);
        safeAddModule("AntiAim", AntiAim::new);
        safeAddModule("ArmorAlert", ArmorAlert::new);
        safeAddModule("AutoSurround", AutoSurround::new);
        safeAddModule("HoleFiller", HoleFiller::new);

        // New interaction/world modules
        safeAddModule("AirPlace", AirPlace::new);
        safeAddModule("GhostHand", GhostHand::new);
        safeAddModule("MultiTask", MultiTask::new);
        safeAddModule("FindStronghold", FindStronghold::new);

        // New movement modules
        safeAddModule("IceSpeed", IceSpeed::new);
        safeAddModule("WaterSpeed", WaterSpeed::new);
        safeAddModule("ElytraTweaks", ElytraTweaks::new);

        // New render/utility modules
        safeAddModule("PortalGodMode", PortalGodMode::new);
        safeAddModule("PortalGui", PortalGui::new);
        safeAddModule("RainbowEnchant", RainbowEnchant::new);
        safeAddModule("FancyChat", FancyChat::new);

        safeAddModule("AntiCrash", AntiCrash::new);
        safeAddModule("AntiVanish", AntiVanish::new);
        safeAddModule("ArrowDmg", ArrowDmg::new);
        safeAddModule("GhostMode", GhostMode::new);
        safeAddModule("KnockbackPlus", KnockbackPlus::new);
        safeAddModule("ShieldBypass", ShieldBypass::new);

        // Testing modules (only visible when Beta Tester mode is enabled)
        safeAddModule("BetaTesterInfo", BetaTesterInfo::new);
        safeAddModule("SpawnTestPlatform", SpawnTestPlatform::new);
        safeAddModule("SpawnTestMobs", SpawnTestMobs::new);
        safeAddModule("SpawnPowderSnow", SpawnPowderSnow::new);
        safeAddModule("TimeWeatherControl", TimeWeatherControl::new);
        safeAddModule("HotbarPresets", HotbarPresets::new);
        safeAddModule("GiveArmorSet", GiveArmorSet::new);
        safeAddModule("HealAndFeed", HealAndFeed::new);
        safeAddModule("GameModeSwitch", GameModeSwitch::new);
        safeAddModule("ClearEffects", ClearEffects::new);
        safeAddModule("GiveEffects", GiveEffects::new);
        safeAddModule("TeleportUtils", TeleportUtils::new);
        safeAddModule("GiveXP", GiveXP::new);
        safeAddModule("BuildStructures", BuildStructures::new);
        safeAddModule("EntityKill", EntityKill::new);

        // Settings modules
        safeAddModule("BetaTesterMode", BetaTesterMode::new);
        safeAddModule("UIMode", UIMode::new);

        // Client Settings (must be last so all modules are registered first)
        safeAddModule("ClientSettings", ClientSettings::new);
    }

    private void safeAddModule(String name, Supplier<Module> supplier) {
        StartupLogger.log("Initializing module: " + name);
        try {
            addModule(supplier.get());
        } catch (Exception e) {
            StartupLogger.logError("Failed to initialize module: " + name, e);
        }
    }

    public void addModule(Module module) {
        if (module == null) {
            return;
        }
        StartupLogger.log("Loading module: " + module.getName() + " (" + module.getClass().getSimpleName() + ")");
        modules.add(module);
        moduleCache.put(module.getClass(), module);

        // Only add to category cache if visible
        if (module.isVisible()) {
            List<Module> list = categoryCache.computeIfAbsent(module.getCategory(), k -> new ArrayList<>());
            list.add(module);
            list.sort(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
        }

        StartupLogger.log("Loaded module: " + module.getName());
    }

    public List<Module> getModules() {
        return modules;
    }

    public static String searchQuery = "";

    public List<Module> getModulesByCategory(Category category) {
        if (category == Category.SEARCH) {
            List<Module> list = new ArrayList<>();
            if (searchQuery.isEmpty()) return list;
            String q = searchQuery.toLowerCase();
            for (Module m : modules) {
                // Filter invisible modules from search
                if (m.isVisible() && m.getName().toLowerCase().contains(q)) {
                    list.add(m);
                }
            }
            // Sort search results alphabetically
            list.sort(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
            return list;
        }

        return categoryCache.getOrDefault(category, Collections.emptyList());
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return clazz.cast(moduleCache.get(clazz));
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void onRender(DrawContext context) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRender(context);
            }
        }
    }

    public void onWorldRender(net.minecraft.client.util.math.MatrixStack matrices) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onWorldRender(matrices);
            }
        }
    }
}
