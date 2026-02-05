package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.ActionSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.ui.screen.ListEditorScreen;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockESP extends Module {
    public static List<BlockPos> foundBlocks = new ArrayList<>();
    private int timer = 0;
    private int scanY = 0; // Current Y layer being scanned for incremental updates
    private BlockPos lastPlayerPos = null;
    private ActionSetting editList = new ActionSetting("Edit List...", () -> {
        MinecraftClient.getInstance().setScreen(new ListEditorScreen(null, new ArrayList<>(this.blockList), list -> {
            this.blockList.clear();
            this.blockList.addAll(list);
            this.rebuildBlockCache();
        }));
    });
    private NumberSetting range = new NumberSetting("Range", 16, 5, 32, 1); // Reduced max to 32 for performance
    private NumberSetting updateInterval = new NumberSetting("Update Ticks", 40, 10, 200, 10); // Configurable update interval
    private Set<String> blockList = new HashSet<>();
    private Set<Block> renderBlocks = new HashSet<>();

    public BlockESP() {
        super("BlockESP", "Highlights specific block types like ores through walls.", Category.RENDER);
        addSetting(editList);
        addSetting(range);
        addSetting(updateInterval);
        blockList.add("minecraft:diamond_ore");
        rebuildBlockCache();
    }

    private void rebuildBlockCache() {
        renderBlocks.clear();
        for (String id : blockList) {
            if (id == null || id.isEmpty()) continue;
            Identifier identifier = Identifier.tryParse(id.contains(":") ? id : "minecraft:" + id);
            if (identifier != null && Registries.BLOCK.containsId(identifier)) {
                renderBlocks.add(Registries.BLOCK.get(identifier));
            }
        }
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        timer++;
        int updateTicks = (int) updateInterval.getValue();
        
        // Only do full scan when timer expires or player moved significantly
        BlockPos currentPos = mc.player.getBlockPos();
        boolean playerMoved = lastPlayerPos == null || currentPos.getSquaredDistance(lastPlayerPos) > 16;
        
        if (timer > updateTicks || playerMoved) {
            // Perform incremental scanning - only scan a few Y layers per tick to spread load
            if (scanY == 0 || playerMoved) {
                foundBlocks.clear();
                lastPlayerPos = currentPos;
            }
            
            int r = (int) range.getValue();
            BlockPos p = mc.player.getBlockPos();
            
            if (renderBlocks.isEmpty()) {
                if (!blockList.isEmpty()) rebuildBlockCache(); // Retry cache build if needed
                if (renderBlocks.isEmpty()) {
                    timer = 0;
                    scanY = 0;
                    return;
                }
            }

            // Scan 4 Y layers per tick to spread the load
            int layersPerTick = 4;
            int startY = -r + scanY;
            int endY = Math.min(r, startY + layersPerTick - 1);
            
            for (int y = startY; y <= endY; y++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = p.add(x, y, z);
                        Block block = mc.world.getBlockState(pos).getBlock();

                        if (renderBlocks.contains(block)) {
                            foundBlocks.add(pos);
                        }
                    }
                }
            }
            
            scanY += layersPerTick;
            if (scanY > 2 * r) {
                scanY = 0;
                timer = 0;
            }
        }
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float r = 0f, g = 1f, b = 1f, a = 1f;

        for (BlockPos pos : foundBlocks) {
            double x = pos.getX() - camPos.x;
            double y = pos.getY() - camPos.y;
            double z = pos.getZ() - camPos.z;

            // Box
            buffer.vertex(matrix, (float)x, (float)y, (float)z).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x+1), (float)y, (float)z).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x+1), (float)y, (float)z).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x+1), (float)y, (float)(z+1)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x+1), (float)y, (float)(z+1)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)x, (float)y, (float)(z+1)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)x, (float)y, (float)(z+1)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)x, (float)y, (float)z).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)x, (float)(y+1), (float)z).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x+1), (float)(y+1), (float)z).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x+1), (float)(y+1), (float)z).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x+1), (float)(y+1), (float)(z+1)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x+1), (float)(y+1), (float)(z+1)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)x, (float)(y+1), (float)(z+1)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)x, (float)(y+1), (float)(z+1)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)x, (float)(y+1), (float)z).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)x, (float)y, (float)z).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)x, (float)(y+1), (float)z).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x+1), (float)y, (float)z).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x+1), (float)(y+1), (float)z).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)(x+1), (float)y, (float)(z+1)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)(x+1), (float)(y+1), (float)(z+1)).color(r,g,b,a).next();

            buffer.vertex(matrix, (float)x, (float)y, (float)(z+1)).color(r,g,b,a).next();
            buffer.vertex(matrix, (float)x, (float)(y+1), (float)(z+1)).color(r,g,b,a).next();
        }

        tessellator.draw();
    }
}
