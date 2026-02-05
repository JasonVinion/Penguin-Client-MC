package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;

public class ChestESP extends Module {
    private BooleanSetting chests = new BooleanSetting("Chests", true);
    private BooleanSetting trappedChests = new BooleanSetting("Trapped Chests", true);
    private BooleanSetting enderChests = new BooleanSetting("Ender Chests", true);
    private BooleanSetting shulkers = new BooleanSetting("Shulkers", true);
    private BooleanSetting minecartChests = new BooleanSetting("Minecart Chests", true);
    private BooleanSetting boatChests = new BooleanSetting("Boat Chests", true);
    private BooleanSetting showType = new BooleanSetting("Show Type", false);
    private NumberSetting updateInterval = new NumberSetting("Update Ticks", 20, 5, 100, 5);
    
    private int tickCounter = 0;
    private List<ChestInfo> cachedChests = new ArrayList<>();
    private List<Box> cachedEntityBoxes = new ArrayList<>();
    private List<int[]> cachedEntityColors = new ArrayList<>();
    private List<String> cachedEntityTypes = new ArrayList<>();
    
    // Class to store chest info including type
    private static class ChestInfo {
        BlockPos pos;
        int[] rgb;
        String type;
        
        ChestInfo(BlockPos pos, int[] rgb, String type) {
            this.pos = pos;
            this.rgb = rgb;
            this.type = type;
        }
    }

    public ChestESP() {
        super("ChestESP", "Highlights storage containers including boat and minecart chests. Can show chest type.", Category.RENDER);
        addSetting(chests);
        addSetting(trappedChests);
        addSetting(enderChests);
        addSetting(shulkers);
        addSetting(minecartChests);
        addSetting(boatChests);
        addSetting(showType);
        addSetting(updateInterval);
    }
    
    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        
        tickCounter++;
        int updateTicks = (int) updateInterval.getValue();
        
        if (tickCounter >= updateTicks) {
            tickCounter = 0;
            cachedChests.clear();
            cachedEntityBoxes.clear();
            cachedEntityColors.clear();
            cachedEntityTypes.clear();
            
            int range = mc.options.getViewDistance().getValue();
            int px = mc.player.getChunkPos().x;
            int pz = mc.player.getChunkPos().z;

            // Scan block entities (regular chests, ender chests, shulkers, barrels)
            for (int x = px - range; x <= px + range; x++) {
                for (int z = pz - range; z <= pz + range; z++) {
                    WorldChunk chunk = mc.world.getChunkManager().getChunk(x, z, net.minecraft.world.chunk.ChunkStatus.FULL, false);
                    if (chunk != null) {
                        for (BlockEntity be : chunk.getBlockEntities().values()) {
                            boolean render = false;
                            int red = 255, green = 255, blue = 255;
                            String chestType = "";

                            if (be instanceof ChestBlockEntity) {
                                // Check if it's a trapped chest by looking at the block type
                                if (mc.world.getBlockState(be.getPos()).getBlock() == Blocks.TRAPPED_CHEST) {
                                    if (trappedChests.isEnabled()) {
                                        render = true; red = 255; green = 100; blue = 100; // Red-ish for trapped
                                        chestType = "Trapped";
                                    }
                                } else if (chests.isEnabled()) {
                                    render = true; red = 255; green = 255; blue = 0; // Yellow
                                    chestType = "Chest";
                                }
                            } else if (be instanceof BarrelBlockEntity) {
                                if (chests.isEnabled()) {
                                    render = true; red = 255; green = 255; blue = 0; // Yellow
                                    chestType = "Barrel";
                                }
                            } else if (be instanceof EnderChestBlockEntity) {
                                if (enderChests.isEnabled()) {
                                    render = true; red = 128; green = 0; blue = 255; // Purple
                                    chestType = "Ender";
                                }
                            } else if (be instanceof ShulkerBoxBlockEntity) {
                                if (shulkers.isEnabled()) {
                                    render = true; red = 255; green = 0; blue = 255; // Magenta
                                    chestType = "Shulker";
                                }
                            }

                            if (render) {
                                cachedChests.add(new ChestInfo(be.getPos(), new int[]{red, green, blue}, chestType));
                            }
                        }
                    }
                }
            }
            
            // Scan entities for chest minecarts and chest boats
            for (Entity entity : mc.world.getEntities()) {
                boolean render = false;
                int red = 255, green = 255, blue = 255;
                String entityType = "";
                
                if (entity instanceof ChestMinecartEntity) {
                    if (minecartChests.isEnabled()) {
                        render = true; red = 255; green = 128; blue = 0; // Orange
                        entityType = "Minecart";
                    }
                } else if (entity instanceof ChestBoatEntity) {
                    if (boatChests.isEnabled()) {
                        render = true; red = 139; green = 90; blue = 43; // Brown
                        entityType = "Boat";
                    }
                }
                
                if (render) {
                    cachedEntityBoxes.add(entity.getBoundingBox());
                    cachedEntityColors.add(new int[]{red, green, blue});
                    cachedEntityTypes.add(entityType);
                }
            }
        }
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        if (cachedChests.isEmpty() && cachedEntityBoxes.isEmpty()) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Render block entity boxes
        for (ChestInfo info : cachedChests) {
            BlockPos pos = info.pos;
            int[] rgb = info.rgb;
            float r = rgb[0] / 255f;
            float g = rgb[1] / 255f;
            float b = rgb[2] / 255f;
            drawBox(buffer, matrix, pos, camPos, r, g, b, 1.0f);
        }
        
        // Render entity boxes
        for (int i = 0; i < cachedEntityBoxes.size(); i++) {
            Box box = cachedEntityBoxes.get(i);
            int[] rgb = cachedEntityColors.get(i);
            float r = rgb[0] / 255f;
            float g = rgb[1] / 255f;
            float b = rgb[2] / 255f;
            drawEntityBox(buffer, matrix, box, camPos, r, g, b, 1.0f);
        }
        
        tessellator.draw();
    }

    private void drawBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3d camPos, float r, float g, float b, float a) {
        double x = pos.getX() - camPos.x;
        double y = pos.getY() - camPos.y;
        double z = pos.getZ() - camPos.z;

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
    
    private void drawEntityBox(BufferBuilder buffer, Matrix4f matrix, Box box, Vec3d camPos, float r, float g, float b, float a) {
        double minX = box.minX - camPos.x;
        double minY = box.minY - camPos.y;
        double minZ = box.minZ - camPos.z;
        double maxX = box.maxX - camPos.x;
        double maxY = box.maxY - camPos.y;
        double maxZ = box.maxZ - camPos.z;

        // Bottom face
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r,g,b,a).next();

        // Top face
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r,g,b,a).next();

        // Vertical edges
        buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r,g,b,a).next();

        buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r,g,b,a).next();
        buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r,g,b,a).next();
    }
}
