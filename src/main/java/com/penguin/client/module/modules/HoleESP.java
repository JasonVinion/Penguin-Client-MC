package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ColorSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import com.mojang.blaze3d.systems.RenderSystem;

import java.awt.Color;

/**
 * HoleESP - Highlights valid holes for crystal combat (obsidian/bedrock holes).
 */
public class HoleESP extends Module {
    private BooleanSetting obsidian = new BooleanSetting("Obsidian Holes", true);
    private BooleanSetting bedrock = new BooleanSetting("Bedrock Holes", true);
    private BooleanSetting mixed = new BooleanSetting("Mixed Holes", true);
    private ColorSetting obsidianColor = new ColorSetting("Obsidian Color", 0x64FF0000); // ARGB: semi-transparent red
    private ColorSetting bedrockColor = new ColorSetting("Bedrock Color", 0x6400FF00); // ARGB: semi-transparent green
    private ColorSetting mixedColor = new ColorSetting("Mixed Color", 0x64FFFF00); // ARGB: semi-transparent yellow
    private NumberSetting range = new NumberSetting("Range", 10.0, 5.0, 50.0, 1.0);
    private BooleanSetting fill = new BooleanSetting("Fill", true);
    private BooleanSetting outline = new BooleanSetting("Outline", true);

    public HoleESP() {
        super("HoleESP", "Highlights valid holes for crystal combat.", Category.RENDER);
        addSetting(obsidian);
        addSetting(bedrock);
        addSetting(mixed);
        addSetting(obsidianColor);
        addSetting(bedrockColor);
        addSetting(mixedColor);
        addSetting(range);
        addSetting(fill);
        addSetting(outline);
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) range.getValue();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Render filled quads first
        if (fill.isEnabled()) {
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        
                        if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getValue() * range.getValue()) {
                            continue;
                        }

                        HoleType type = getHoleType(pos);
                        if (type == HoleType.NONE) continue;

                        Color color = getColorForHole(type);
                        if (color == null) continue;

                        renderFilledBox(buffer, matrix, pos, cameraPos, color);
                    }
                }
            }
            
            tessellator.draw();
        }

        // Render outlines
        if (outline.isEnabled()) {
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        
                        if (mc.player.squaredDistanceTo(pos.toCenterPos()) > range.getValue() * range.getValue()) {
                            continue;
                        }

                        HoleType type = getHoleType(pos);
                        if (type == HoleType.NONE) continue;

                        Color color = getColorForHole(type);
                        if (color == null) continue;

                        renderOutlineBox(buffer, matrix, pos, cameraPos, color);
                    }
                }
            }
            
            tessellator.draw();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private HoleType getHoleType(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // Check if center is air or replaceable
        if (!mc.world.getBlockState(pos).isAir() && !mc.world.getBlockState(pos).isReplaceable()) {
            return HoleType.NONE;
        }

        // Check if there's air above (at least 2 blocks)
        if (!mc.world.getBlockState(pos.up()).isAir() || !mc.world.getBlockState(pos.up(2)).isAir()) {
            return HoleType.NONE;
        }

        boolean hasObsidian = false;
        boolean hasBedrock = false;

        // Check all horizontal directions and floor
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN}) {
            BlockPos neighborPos = pos.offset(dir);
            Block block = mc.world.getBlockState(neighborPos).getBlock();
            
            if (block == Blocks.BEDROCK) {
                hasBedrock = true;
            } else if (block == Blocks.OBSIDIAN) {
                hasObsidian = true;
            } else {
                // If any side is not obsidian or bedrock, it's not a valid hole
                return HoleType.NONE;
            }
        }

        // Determine hole type
        if (hasBedrock && hasObsidian) {
            return mixed.isEnabled() ? HoleType.MIXED : HoleType.NONE;
        } else if (hasBedrock) {
            return bedrock.isEnabled() ? HoleType.BEDROCK : HoleType.NONE;
        } else if (hasObsidian) {
            return obsidian.isEnabled() ? HoleType.OBSIDIAN : HoleType.NONE;
        }

        return HoleType.NONE;
    }

    private Color getColorForHole(HoleType type) {
        int colorInt;
        switch (type) {
            case OBSIDIAN:
                colorInt = obsidianColor.getColor();
                break;
            case BEDROCK:
                colorInt = bedrockColor.getColor();
                break;
            case MIXED:
                colorInt = mixedColor.getColor();
                break;
            default:
                return null;
        }
        
        // Extract ARGB components from int
        int a = (colorInt >> 24) & 0xFF;
        int r = (colorInt >> 16) & 0xFF;
        int g = (colorInt >> 8) & 0xFF;
        int b = colorInt & 0xFF;
        return new Color(r, g, b, a);
    }

    private void renderFilledBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3d cameraPos, Color color) {
        float x = (float) (pos.getX() - cameraPos.x);
        float y = (float) (pos.getY() - cameraPos.y);
        float z = (float) (pos.getZ() - cameraPos.z);
        
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;
        
        // Flat box at bottom of hole (height 0.1)
        float h = 0.1f;
        
        // Top face (visible from above)
        buffer.vertex(matrix, x, y + h, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y + h, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y + h, z + 1).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y + h, z + 1).color(r, g, b, a).next();
    }

    private void renderOutlineBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, Vec3d cameraPos, Color color) {
        float x = (float) (pos.getX() - cameraPos.x);
        float y = (float) (pos.getY() - cameraPos.y);
        float z = (float) (pos.getZ() - cameraPos.z);
        
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = Math.min(1.0f, (color.getAlpha() / 255.0f) * 2); // Make outline more visible
        
        float h = 0.1f;
        
        // Bottom outline
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y, z).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x + 1, y, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y, z + 1).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x + 1, y, z + 1).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y, z + 1).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x, y, z + 1).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).next();
        
        // Top outline
        buffer.vertex(matrix, x, y + h, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y + h, z).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x + 1, y + h, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y + h, z + 1).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x + 1, y + h, z + 1).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y + h, z + 1).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x, y + h, z + 1).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y + h, z).color(r, g, b, a).next();
        
        // Vertical edges (4 corners)
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y + h, z).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x + 1, y, z).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y + h, z).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x + 1, y, z + 1).color(r, g, b, a).next();
        buffer.vertex(matrix, x + 1, y + h, z + 1).color(r, g, b, a).next();
        
        buffer.vertex(matrix, x, y, z + 1).color(r, g, b, a).next();
        buffer.vertex(matrix, x, y + h, z + 1).color(r, g, b, a).next();
    }

    private enum HoleType {
        NONE,
        OBSIDIAN,
        BEDROCK,
        MIXED
    }
}
