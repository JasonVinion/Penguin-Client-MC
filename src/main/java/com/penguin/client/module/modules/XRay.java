package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.module.ModuleManager;
import com.penguin.client.settings.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.FlowerBlock;

public class XRay extends Module {

    private static XRay INSTANCE;

    public BooleanSetting coal = new BooleanSetting("Coal", true);
    public BooleanSetting iron = new BooleanSetting("Iron", true);
    public BooleanSetting gold = new BooleanSetting("Gold", true);
    public BooleanSetting diamond = new BooleanSetting("Diamond", true);
    public BooleanSetting emerald = new BooleanSetting("Emerald", true);
    public BooleanSetting redstone = new BooleanSetting("Redstone", true);
    public BooleanSetting lapis = new BooleanSetting("Lapis", true);
    public BooleanSetting netherite = new BooleanSetting("Netherite", true);
    public BooleanSetting copper = new BooleanSetting("Copper", true);
    public BooleanSetting falling = new BooleanSetting("Falling Blocks", false);
    public BooleanSetting liquids = new BooleanSetting("Liquids", false);
    public BooleanSetting hidePartialBlocks = new BooleanSetting("Hide Partial Blocks", true);

    public XRay() {
        super("XRay", "Makes most blocks invisible to reveal ores and valuable blocks.", Category.RENDER);
        addSetting(coal);
        addSetting(iron);
        addSetting(copper);
        addSetting(gold);
        addSetting(diamond);
        addSetting(emerald);
        addSetting(redstone);
        addSetting(lapis);
        addSetting(netherite);
        addSetting(falling);
        addSetting(liquids);
        addSetting(hidePartialBlocks);
        INSTANCE = this;
    }

    public static boolean isEnabledStatic() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
    
    public static boolean shouldHidePartialBlocks() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.hidePartialBlocks.isEnabled();
    }

    public static boolean isVisible(Block block) {
        if (INSTANCE == null) return false;
        
        // Hide partial blocks like snow layers, carpets, vines, flowers, tall plants
        if (INSTANCE.hidePartialBlocks.isEnabled()) {
            // Hide BOTH full snow blocks AND the partial snow layers
            if (block == Blocks.SNOW_BLOCK ||  // Full snow block
                block == Blocks.SNOW ||  // Partial snow layers that accumulate on blocks from snow weather
                block instanceof SnowBlock ||  // Any snow block variants
                block instanceof CarpetBlock || 
                block instanceof TallPlantBlock ||
                block instanceof VineBlock ||
                block instanceof FlowerBlock ||
                block == Blocks.GRASS ||
                block == Blocks.TALL_GRASS ||
                block == Blocks.FERN ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.DEAD_BUSH ||
                block == Blocks.SEAGRASS ||
                block == Blocks.TALL_SEAGRASS ||
                block == Blocks.KELP ||
                block == Blocks.KELP_PLANT ||
                block == Blocks.POWDER_SNOW) {  // Also hide powder snow
                return false;
            }
        }

        if (INSTANCE.falling.isEnabled() && block instanceof net.minecraft.block.FallingBlock) return true;
        if (INSTANCE.liquids.isEnabled() && (block instanceof net.minecraft.block.FluidBlock || block == Blocks.WATER || block == Blocks.LAVA)) return true;

        if (INSTANCE.coal.isEnabled() && (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE)) return true;
        if (INSTANCE.iron.isEnabled() && (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)) return true;
        if (INSTANCE.copper.isEnabled() && (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)) return true;
        if (INSTANCE.gold.isEnabled() && (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)) return true;
        if (INSTANCE.diamond.isEnabled() && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)) return true;
        if (INSTANCE.emerald.isEnabled() && (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)) return true;
        if (INSTANCE.redstone.isEnabled() && (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)) return true;
        if (INSTANCE.lapis.isEnabled() && (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)) return true;
        if (INSTANCE.netherite.isEnabled() && (block == Blocks.ANCIENT_DEBRIS)) return true;

        return false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        // Trigger a render reload to apply XRay instantly?
        // mc.worldRenderer.reload();
        // Accessing mc from static context needs care.
        // For now, user might need to reload chunks manually or I can trigger it.
        if (net.minecraft.client.MinecraftClient.getInstance().worldRenderer != null) {
            net.minecraft.client.MinecraftClient.getInstance().worldRenderer.reload();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (net.minecraft.client.MinecraftClient.getInstance().worldRenderer != null) {
            net.minecraft.client.MinecraftClient.getInstance().worldRenderer.reload();
        }
    }
}
