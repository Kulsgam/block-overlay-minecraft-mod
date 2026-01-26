// Portions of this file are adapted from https://github.com/An0mz/BlockOutlineCustomizer/blob/e78b6da2513410d9f4377cacd6a1471d607f0870/Fabric/src/main/java/me/anomz/blockoutline/fabric/mixin/LevelRendererMixin.java
// Licensed under the MIT License.
// See THIRD_PARTY_LICENSES for details.

package com.kulsgam.mixin;

import com.kulsgam.BlockOverlayClient;
import com.kulsgam.config.BlockOverlayConfig;
import com.kulsgam.config.RenderSettings;
import com.kulsgam.utils.ColorUtils;
import com.kulsgam.utils.RenderUtils;
import com.kulsgam.utils.enums.RenderMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void onRenderOverlay(
            MatrixStack matrices,
            VertexConsumer vertexConsumer,
            double camX,
            double camY,
            double camZ,
            OutlineRenderState blockOutlineRenderState,
            int color,
            float lineWidth,
            CallbackInfo ci
    ) {
        BlockPos blockPos = blockOutlineRenderState.pos();
        VoxelShape shape = blockOutlineRenderState.shape();

        if (shape.isEmpty()) {
            BlockOverlayClient.instance.getLogger().debug("Shape is empty");
            return;
        }

        BlockOverlayConfig config = BlockOverlayClient.instance.getConfig();
        RenderMode renderMode = config.renderMode;
        if (renderMode == RenderMode.VANILLA) {
            return;
        }
        ci.cancel();
        if (renderMode == RenderMode.HIDDEN) {
            return;
        }

        Direction side = null;
        if (renderMode == RenderMode.SIDE) {
            HitResult hitResult = BlockOverlayClient.instance.getClient().crosshairTarget;
            if (hitResult instanceof BlockHitResult blockHitResult) {
                side = blockHitResult.getSide();
            } else {
                return;
            }
        }
        RenderSettings fillSettings = config.fillRender;
        RenderSettings outlineSettings = config.outlineRender;

        matrices.push();
        matrices.translate(
                (double) blockPos.getX() - camX,
                (double) blockPos.getY() - camY,
                (double) blockPos.getZ() - camZ
        );

        // Get buffer source - we need to get both consumers from here
        VertexConsumerProvider.Immediate bufferSource = BlockOverlayClient.instance.getClient().
                getBufferBuilders().getEntityVertexConsumers();

        // Render fill first (if enabled)
        if (fillSettings.visible) {
            renderFill(bufferSource, shape, matrices, fillSettings, side);
        }

        // Render outline - get our own line consumer since we cancelled vanilla setup
        if (outlineSettings.visible) {
            renderOutline(bufferSource, shape, matrices, outlineSettings, config.thickness, side);
        }

        matrices.pop();
    }

    @Unique
    private void renderOutline(VertexConsumerProvider.Immediate bufferSource, VoxelShape shape, MatrixStack matrices,
                               RenderSettings outlineSettings, double thickness, Direction side) {
        int startColor = outlineSettings.getStart();
        int endColor = outlineSettings.getEnd();
        float lineWidth = (float) thickness;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Box bounds = shape.getBoundingBox();
        double minY = bounds.minY;
        double maxY = bounds.maxY;

        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderLayers.lines());

        int passes = Math.max(1, (int) lineWidth);
        float offsetIncrement = 0.001f;

        for (int pass = 0; pass < passes; pass++) {
            float offset = pass * offsetIncrement;
            if (side == null) {
                shape.forEachEdge((edgeMinX, edgeMinY, edgeMinZ, edgeMaxX, edgeMaxY, edgeMaxZ) -> {
                    float minX = (float) edgeMinX + offset;
                    float minYEdge = (float) edgeMinY + offset;
                    float minZ = (float) edgeMinZ + offset;
                    float maxX = (float) edgeMaxX + offset;
                    float maxYEdge = (float) edgeMaxY + offset;
                    float maxZ = (float) edgeMaxZ + offset;

                    int minColor = colorForY(startColor, endColor, minYEdge, minY, maxY);
                    int maxColor = colorForY(startColor, endColor, maxYEdge, minY, maxY);
                    emitLine(lineConsumer, matrix, minX, minYEdge, minZ, minColor, maxX, maxYEdge, maxZ, maxColor, lineWidth);
                });
            } else {
                shape.forEachBox((boxMinX, boxMinY, boxMinZ, boxMaxX, boxMaxY, boxMaxZ) -> {
                    Box box = new Box(boxMinX, boxMinY, boxMinZ, boxMaxX, boxMaxY, boxMaxZ);
                    drawFaceOutline(lineConsumer, matrix, box, side, startColor, endColor, minY, maxY, lineWidth, offset);
                });
            }
        }
    }

    @Unique
    private void renderFill(VertexConsumerProvider.Immediate bufferSource, VoxelShape shape, MatrixStack matrices,
                            RenderSettings fillSettings, Direction side) {
        int startColor = fillSettings.getStart();
        int endColor = fillSettings.getEnd();

        RenderLayer tmp = RenderLayer.of("lightning", RenderSetup.builder(RenderPipelines.RENDERTYPE_LIGHTNING).outputTarget(OutputTarget.MAIN_TARGET).translucent().build());
        VertexConsumer fillConsumer = bufferSource.getBuffer(tmp);
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ).expand(0.001);
            RenderUtils.emitBoxQuads(matrices, fillConsumer, box, side, startColor, endColor);
        });
    }

    @Unique
    private void emitLine(VertexConsumer lineConsumer, Matrix4f matrix,
                          float x1, float y1, float z1, int c1,
                          float x2, float y2, float z2, int c2,
                          float lineWidth) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float normalX = length > 1e-6f ? dx / length : 1.0f;
        float normalY = length > 1e-6f ? dy / length : 0.0f;
        float normalZ = length > 1e-6f ? dz / length : 0.0f;

        float[] rgba1 = ColorUtils.toRgba(c1);
        float[] rgba2 = ColorUtils.toRgba(c2);

        lineConsumer.vertex(matrix, x1, y1, z1)
                .color(rgba1[0], rgba1[1], rgba1[2], rgba1[3])
                .normal(normalX, normalY, normalZ)
                .lineWidth(lineWidth);
        lineConsumer.vertex(matrix, x2, y2, z2)
                .color(rgba2[0], rgba2[1], rgba2[2], rgba2[3])
                .normal(normalX, normalY, normalZ)
                .lineWidth(lineWidth);
    }

    @Unique
    private void drawFaceOutline(VertexConsumer lineConsumer, Matrix4f matrix, Box box, Direction side,
                                 int startColor, int endColor, double minY, double maxY,
                                 float lineWidth, float offset) {
        float minX = (float) box.minX + offset;
        float minYBox = (float) box.minY + offset;
        float minZ = (float) box.minZ + offset;
        float maxX = (float) box.maxX + offset;
        float maxYBox = (float) box.maxY + offset;
        float maxZ = (float) box.maxZ + offset;

        float[][] corners = switch (side) {
            case UP -> new float[][]{
                    {minX, maxYBox, minZ}, {maxX, maxYBox, minZ}, {maxX, maxYBox, maxZ}, {minX, maxYBox, maxZ}
            };
            case DOWN -> new float[][]{
                    {minX, minYBox, minZ}, {maxX, minYBox, minZ}, {maxX, minYBox, maxZ}, {minX, minYBox, maxZ}
            };
            case NORTH -> new float[][]{
                    {minX, minYBox, minZ}, {maxX, minYBox, minZ}, {maxX, maxYBox, minZ}, {minX, maxYBox, minZ}
            };
            case SOUTH -> new float[][]{
                    {maxX, minYBox, maxZ}, {minX, minYBox, maxZ}, {minX, maxYBox, maxZ}, {maxX, maxYBox, maxZ}
            };
            case WEST -> new float[][]{
                    {minX, minYBox, maxZ}, {minX, minYBox, minZ}, {minX, maxYBox, minZ}, {minX, maxYBox, maxZ}
            };
            case EAST -> new float[][]{
                    {maxX, minYBox, minZ}, {maxX, minYBox, maxZ}, {maxX, maxYBox, maxZ}, {maxX, maxYBox, minZ}
            };
        };

        for (int i = 0; i < corners.length; i++) {
            float[] start = corners[i];
            float[] end = corners[(i + 1) % corners.length];
            int startColorEdge = colorForY(startColor, endColor, start[1], minY, maxY);
            int endColorEdge = colorForY(startColor, endColor, end[1], minY, maxY);
            emitLine(lineConsumer, matrix, start[0], start[1], start[2], startColorEdge,
                    end[0], end[1], end[2], endColorEdge, lineWidth);
        }
    }

    @Unique
    private int colorForY(int startColor, int endColor, double y, double minY, double maxY) {
        double range = Math.max(1e-6, maxY - minY);
        float t = (float) ((y - minY) / range);
        return lerpColor(startColor, endColor, t);
    }

    @Unique
    private int lerpColor(int c0, int c1, float t) {
        int a0 = (c0 >>> 24) & 0xFF;
        int r0 = (c0 >>> 16) & 0xFF;
        int g0 = (c0 >>> 8) & 0xFF;
        int b0 = c0 & 0xFF;
        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int a = (int) (a0 + (a1 - a0) * t);
        int r = (int) (r0 + (r1 - r0) * t);
        int g = (int) (g0 + (g1 - g0) * t);
        int b = (int) (b0 + (b1 - b0) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
