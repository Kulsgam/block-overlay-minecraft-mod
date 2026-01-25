package com.kulsgam.utils;

import java.awt.Color;

import net.minecraft.client.render.*;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.slf4j.Logger;

public class RenderUtils {
    private static final Tessellator TESSELLATOR = Tessellator.getInstance();

    public static void drawBlock(
            MatrixStack matrices,
            Box worldBox,
            @Nullable Direction side,
            int overlayStartColor, int overlayEndColor,
            int outlineStartColor, int outlineEndColor,
            boolean overlay, boolean outline,
            @Nullable VertexConsumer fillConsumer,
            @Nullable VertexConsumer lineConsumer,
            Logger logger
    ) {
        if (overlay && fillConsumer != null) {
            emitBoxQuads(matrices, fillConsumer, worldBox, side, overlayStartColor, overlayEndColor);
        }
        if (outline && lineConsumer != null) {
            emitBoxLines(matrices, lineConsumer, worldBox, outlineStartColor, outlineEndColor);
        }
    }

    private static void drawBlockFull(MatrixStack matrices, Box box, Color overlayStartColor, Color overlayEndColor,
                                      Color outlineStartColor, Color outlineEndColor, boolean overlay, boolean outline, Logger logger) {
        if (overlay) {
            drawBlockTop(matrices, box, overlayStartColor, overlayEndColor, true, logger);
            drawBlockBottom(matrices, box, overlayStartColor, overlayEndColor, true, logger);
            drawBlockNorth(matrices, box, overlayStartColor, overlayEndColor, true, logger);
            drawBlockEast(matrices, box, overlayStartColor, overlayEndColor, true, logger);
            drawBlockSouth(matrices, box, overlayStartColor, overlayEndColor, true, logger);
            drawBlockWest(matrices, box, overlayStartColor, overlayEndColor, true, logger);
        }
        if (outline) {
            drawBlockTop(matrices, box, outlineStartColor, outlineEndColor, false, logger);
            drawBlockBottom(matrices, box, outlineStartColor, outlineEndColor, false, logger);
            drawBlockNorth(matrices, box, outlineStartColor, outlineEndColor, false, logger);
            drawBlockEast(matrices, box, outlineStartColor, outlineEndColor, false, logger);
            drawBlockSouth(matrices, box, outlineStartColor, outlineEndColor, false, logger);
            drawBlockWest(matrices, box, outlineStartColor, outlineEndColor, false, logger);
        }
    }

    private static void drawBlockSide(MatrixStack matrices, Box box, Direction side, Color overlayStartColor,
                                      Color overlayEndColor, Color outlineStartColor, Color outlineEndColor,
                                      boolean overlay, boolean outline, Logger logger) {
        switch (side) {
            case UP -> {
                drawBlockTop(matrices, box, overlayStartColor, overlayEndColor, overlay, logger);
                if (outline) {
                    drawBlockTop(matrices, box, outlineStartColor, outlineEndColor, false, logger);
                }
            }
            case DOWN -> {
                drawBlockBottom(matrices, box, overlayStartColor, overlayEndColor, overlay, logger);
                if (outline) {
                    drawBlockBottom(matrices, box, outlineStartColor, outlineEndColor, false, logger);
                }
            }
            case NORTH -> {
                drawBlockNorth(matrices, box, overlayStartColor, overlayEndColor, overlay, logger);
                if (outline) {
                    drawBlockNorth(matrices, box, outlineStartColor, outlineEndColor, false, logger);
                }
            }
            case EAST -> {
                drawBlockEast(matrices, box, overlayStartColor, overlayEndColor, overlay, logger);
                if (outline) {
                    drawBlockEast(matrices, box, outlineStartColor, outlineEndColor, false, logger);
                }
            }
            case SOUTH -> {
                drawBlockSouth(matrices, box, overlayStartColor, overlayEndColor, overlay, logger);
                if (outline) {
                    drawBlockSouth(matrices, box, outlineStartColor, outlineEndColor, false, logger);
                }
            }
            case WEST -> {
                drawBlockWest(matrices, box, overlayStartColor, overlayEndColor, overlay, logger);
                if (outline) {
                    drawBlockWest(matrices, box, outlineStartColor, outlineEndColor, false, logger);
                }
            }
        }
    }

    private static void drawBlockTop(MatrixStack matrices, Box box, Color startColor, Color endColor, boolean fill, Logger logger) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        if (fill) {
            BufferBuilder BUFFER = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            BUFFER.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            applyBuffer(box, endColor, logger, matrix, BUFFER);
        } else {
            drawLineLoop(matrix, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, startColor, endColor, true, logger);
        }
    }

    private static void applyBuffer(Box box, Color endColor, Logger logger, Matrix4f matrix, BufferBuilder BUFFER) {
        BUFFER.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
        try {
            BuiltBuffer b = BUFFER.endNullable();
            if (b != null) {
                b.close();
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void drawBlockBottom(MatrixStack matrices, Box box, Color startColor, Color endColor, boolean fill, Logger logger) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        if (fill) {
            BufferBuilder BUFFER = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            applyBuffer2(box, endColor, logger, matrix, BUFFER);
        } else {
            drawLineLoop(matrix, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, startColor, endColor, false, logger);
        }
    }

    private static void applyBuffer2(Box box, Color endColor, Logger logger, Matrix4f matrix, BufferBuilder BUFFER) {
        BUFFER.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
        try {
            BuiltBuffer b = BUFFER.endNullable();
            if (b != null) {
                b.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void drawBlockNorth(MatrixStack matrices, Box box, Color startColor, Color endColor, boolean fill, Logger logger) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        if (fill) {
            BufferBuilder BUFFER = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            applyBuffer(box, endColor, logger, matrix, BUFFER);
        } else {
            drawLineLoop(matrix, box.maxX, box.maxY, box.maxZ, box.minX, box.minY, box.maxZ, startColor, endColor, true, logger);
        }
    }

    private static void drawBlockEast(MatrixStack matrices, Box box, Color startColor, Color endColor, boolean fill, Logger logger) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        if (fill) {
            BufferBuilder BUFFER = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            applyBuffer2(box, endColor, logger, matrix, BUFFER);
        } else {
            drawLineLoop(matrix, box.maxX, box.maxY, box.maxZ, box.maxX, box.minY, box.minZ, startColor, endColor, true, logger);
        }
    }

    private static void drawBlockSouth(MatrixStack matrices, Box box, Color startColor, Color endColor, boolean fill, Logger logger) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        if (fill) {
            BufferBuilder BUFFER = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            BUFFER.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            try {
                BuiltBuffer b = BUFFER.endNullable();
                if (b != null) {
                    b.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else {
            drawLineLoop(matrix, box.minX, box.maxY, box.minZ, box.maxX, box.minY, box.minZ, startColor, endColor, true, logger);
        }
    }

    private static void drawBlockWest(MatrixStack matrices, Box box, Color startColor, Color endColor, boolean fill, Logger logger) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        if (fill) {
            BufferBuilder BUFFER = TESSELLATOR.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            BUFFER.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
            BUFFER.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
            try {
                BuiltBuffer b = BUFFER.endNullable();
                if (b != null) {
                    b.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        } else {
            drawLineLoop(matrix, box.minX, box.maxY, box.minZ, box.minX, box.minY, box.maxZ, startColor, endColor, true, logger);
        }
    }

    private static void drawLineLoop(Matrix4f matrix,
                                     double x1, double y1, double z1,
                                     double x2, double y2, double z2,
                                     Color startColor, Color endColor,
                                     boolean reverse, Logger logger) {
        BufferBuilder BUFFER = TESSELLATOR.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
        try {
            if (reverse) {
                applyLineLoop(matrix, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, startColor, endColor, BUFFER);
            } else {
                applyLineLoop(matrix, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z1, startColor, endColor, BUFFER);
            }
            BuiltBuffer b = BUFFER.endNullable();
            if (b != null) {
                b.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void applyLineLoop(Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, Color startColor, Color endColor, BufferBuilder BUFFER) {
        BUFFER.vertex(matrix, x1, y1, z1).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
        BUFFER.vertex(matrix, x2, y1, z1).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
        BUFFER.vertex(matrix, x2, y2, z2).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
        BUFFER.vertex(matrix, x1, y2, z2).color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), endColor.getAlpha());
        BUFFER.vertex(matrix, x1, y1, z1).color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), startColor.getAlpha());
    }


    private static int a(int argb) { return (argb >>> 24) & 0xFF; }
    private static int r(int argb) { return (argb >>> 16) & 0xFF; }
    private static int g(int argb) { return (argb >>>  8) & 0xFF; }
    private static int b(int argb) { return (argb       ) & 0xFF; }

    private static int lerpColor(int c0, int c1, float t) {
        int a = (int)(a(c0) + (a(c1) - a(c0)) * t);
        int r = (int)(r(c0) + (r(c1) - r(c0)) * t);
        int g = (int)(g(c0) + (g(c1) - g(c0)) * t);
        int b = (int)(b(c0) + (b(c1) - b(c0)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Emits filled quads for a box.
     * If side != null, emits only that face.
     * start/end color is applied as a simple Y-gradient (bottom=start, top=end).
     */
    public static void emitBoxQuads(MatrixStack matrices, VertexConsumer vc, Box box, Direction side,
                                    int startArgb, int endArgb) {
        Matrix4f m = matrices.peek().getPositionMatrix();

        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // helper: choose color based on vertex y
        java.util.function.Function<Float, Integer> colY =
                (y) -> lerpColor(startArgb, endArgb, (y - minY) / Math.max(0.00001f, (maxY - minY)));

        // Each face is 4 vertices (quad). Winding doesn’t matter much for debug layers, but keep consistent.

        if (side == null || side == Direction.UP) {
            quad(vc, m, minX, maxY, minZ,  maxX, maxY, minZ,  maxX, maxY, maxZ,  minX, maxY, maxZ,
                    colY.apply(maxY));
        }
        if (side == null || side == Direction.DOWN) {
            quad(vc, m, minX, minY, maxZ,  maxX, minY, maxZ,  maxX, minY, minZ,  minX, minY, minZ,
                    colY.apply(minY));
        }
        if (side == null || side == Direction.NORTH) { // -Z
            quad4(vc, m,
                    minX, minY, minZ, colY.apply(minY),
                    maxX, minY, minZ, colY.apply(minY),
                    maxX, maxY, minZ, colY.apply(maxY),
                    minX, maxY, minZ, colY.apply(maxY));
        }
        if (side == null || side == Direction.SOUTH) { // +Z
            quad4(vc, m,
                    maxX, minY, maxZ, colY.apply(minY),
                    minX, minY, maxZ, colY.apply(minY),
                    minX, maxY, maxZ, colY.apply(maxY),
                    maxX, maxY, maxZ, colY.apply(maxY));
        }
        if (side == null || side == Direction.WEST) { // -X
            quad4(vc, m,
                    minX, minY, maxZ, colY.apply(minY),
                    minX, minY, minZ, colY.apply(minY),
                    minX, maxY, minZ, colY.apply(maxY),
                    minX, maxY, maxZ, colY.apply(maxY));
        }
        if (side == null || side == Direction.EAST) { // +X
            quad4(vc, m,
                    maxX, minY, minZ, colY.apply(minY),
                    maxX, minY, maxZ, colY.apply(minY),
                    maxX, maxY, maxZ, colY.apply(maxY),
                    maxX, maxY, minZ, colY.apply(maxY));
        }
    }

    /** Emits box outline lines (12 edges) */
    public static void emitBoxLines(MatrixStack matrices, VertexConsumer vc, Box box, int argb) {
        Matrix4f m = matrices.peek().getPositionMatrix();

        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // bottom rectangle
        line(vc, m, minX, minY, minZ, maxX, minY, minZ, argb);
        line(vc, m, maxX, minY, minZ, maxX, minY, maxZ, argb);
        line(vc, m, maxX, minY, maxZ, minX, minY, maxZ, argb);
        line(vc, m, minX, minY, maxZ, minX, minY, minZ, argb);

        // top rectangle
        line(vc, m, minX, maxY, minZ, maxX, maxY, minZ, argb);
        line(vc, m, maxX, maxY, minZ, maxX, maxY, maxZ, argb);
        line(vc, m, maxX, maxY, maxZ, minX, maxY, maxZ, argb);
        line(vc, m, minX, maxY, maxZ, minX, maxY, minZ, argb);

        // vertical edges
        line(vc, m, minX, minY, minZ, minX, maxY, minZ, argb);
        line(vc, m, maxX, minY, minZ, maxX, maxY, minZ, argb);
        line(vc, m, maxX, minY, maxZ, maxX, maxY, maxZ, argb);
        line(vc, m, minX, minY, maxZ, minX, maxY, maxZ, argb);
    }

    public static void emitBoxLines(MatrixStack matrices, VertexConsumer vc, Box box, int startArgb, int endArgb) {
        Matrix4f m = matrices.peek().getPositionMatrix();

        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // bottom (use start color)
        line(vc, m, minX, minY, minZ, maxX, minY, minZ, startArgb);
        line(vc, m, maxX, minY, minZ, maxX, minY, maxZ, startArgb);
        line(vc, m, maxX, minY, maxZ, minX, minY, maxZ, startArgb);
        line(vc, m, minX, minY, maxZ, minX, minY, minZ, startArgb);

        // top (use end color)
        line(vc, m, minX, maxY, minZ, maxX, maxY, minZ, endArgb);
        line(vc, m, maxX, maxY, minZ, maxX, maxY, maxZ, endArgb);
        line(vc, m, maxX, maxY, maxZ, minX, maxY, maxZ, endArgb);
        line(vc, m, minX, maxY, maxZ, minX, maxY, minZ, endArgb);

        // vertical edges (blend bottom->top by using start at bottom vertex and end at top vertex)
        line2(vc, m, minX, minY, minZ, startArgb, minX, maxY, minZ, endArgb);
        line2(vc, m, maxX, minY, minZ, startArgb, maxX, maxY, minZ, endArgb);
        line2(vc, m, maxX, minY, maxZ, startArgb, maxX, maxY, maxZ, endArgb);
        line2(vc, m, minX, minY, maxZ, startArgb, minX, maxY, maxZ, endArgb);
    }


    // ---------- tiny primitives ----------

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb) {
        vc.vertex(m, x, y, z).color(r(argb), g(argb), b(argb), a(argb));
    }

    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             int argb) {
        v(vc, m, x1, y1, z1, argb);
        v(vc, m, x2, y2, z2, argb);
        v(vc, m, x3, y3, z3, argb);
        v(vc, m, x4, y4, z4, argb);
    }

    private static void quad4(VertexConsumer vc, Matrix4f m,
                              float x1, float y1, float z1, int c1,
                              float x2, float y2, float z2, int c2,
                              float x3, float y3, float z3, int c3,
                              float x4, float y4, float z4, int c4) {
        v(vc, m, x1, y1, z1, c1);
        v(vc, m, x2, y2, z2, c2);
        v(vc, m, x3, y3, z3, c3);
        v(vc, m, x4, y4, z4, c4);
    }

    private static void line(VertexConsumer vc, Matrix4f m,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             int argb) {
        v(vc, m, x1, y1, z1, argb);
        v(vc, m, x2, y2, z2, argb);
    }

    private static void line2(VertexConsumer vc, Matrix4f m,
                              float x1, float y1, float z1, int c1,
                              float x2, float y2, float z2, int c2) {
        v(vc, m, x1, y1, z1, c1);
        v(vc, m, x2, y2, z2, c2);
    }
}
