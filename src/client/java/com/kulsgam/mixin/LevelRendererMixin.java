// Portions of this file are adapted from https://github.com/An0mz/BlockOutlineCustomizer/blob/e78b6da2513410d9f4377cacd6a1471d607f0870/Fabric/src/main/java/me/anomz/blockoutline/fabric/mixin/LevelRendererMixin.java
// Licensed under the MIT License.
// See THIRD_PARTY_LICENSES for details.

package com.kulsgam.mixin;

import com.kulsgam.BlockOverlayClient;
import com.kulsgam.config.BlockOverlayConfig;
import com.kulsgam.config.RenderSettings;
import com.kulsgam.utils.ColorUtils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "drawBlockOutline", at = @At("TAIL"), cancellable = true)
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
        ci.cancel();

        BlockPos blockPos = blockOutlineRenderState.pos();
        VoxelShape shape = blockOutlineRenderState.shape();

        if (shape.isEmpty()) {
            BlockOverlayClient.instance.getLogger().debug("Shape is empty");
            return;
        }

        BlockOverlayConfig config = BlockOverlayClient.instance.getConfig();
        RenderSettings fillSettings = config.fillRender;
        RenderSettings outlineSettings = config.outlineRender;

        matrices.push();
        matrices.translate(
                (double) blockPos.getX() - camX,
                (double) blockPos.getY() - camY,
                (double) blockPos.getZ() - camZ
        );

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Get buffer source - we need to get both consumers from here
        VertexConsumerProvider.Immediate bufferSource = BlockOverlayClient.instance.getClient().
                getBufferBuilders().getEntityVertexConsumers();

        // Render fill first (if enabled)
        if (fillSettings.visible) {
            renderFill(bufferSource, shape, matrix, fillSettings);
        }

        // Render outline - get our own line consumer since we cancelled vanilla setup
        renderOutline(bufferSource, shape, matrix, outlineSettings, config.thickness);

        matrices.pop();
    }

    @Unique
    private void renderOutline(VertexConsumerProvider.Immediate bufferSource, VoxelShape shape, Matrix4f matrix, RenderSettings outlineSettings, double thickness) {
        float[] rgba = ColorUtils.toRgba(outlineSettings.getStart());
        float red = rgba[0];
        float green = rgba[1];
        float blue = rgba[2];
        float alpha = rgba[3];
        float lineWidth = (float) thickness;

        // Get our own vertex consumer for lines
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderLayers.lines());

        int passes = Math.max(1, (int) lineWidth);
        float offsetIncrement = 0.001f;

        for (int pass = 0; pass < passes; pass++) {
            float offset = pass * offsetIncrement;

            shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
                float dx = (float) (maxX - minX);
                float dy = (float) (maxY - minY);
                float dz = (float) (maxZ - minZ);

                float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                float normalX = length > 1e-6f ? dx / length : 1.0f;
                float normalY = length > 1e-6f ? dy / length : 0.0f;
                float normalZ = length > 1e-6f ? dz / length : 0.0f;

                lineConsumer.vertex(matrix, (float) minX + offset, (float) minY + offset, (float) minZ + offset)
                        .color(red, green, blue, alpha)
                        .normal(normalX, normalY, normalZ)
                        .lineWidth(lineWidth);

                lineConsumer.vertex(matrix, (float) maxX + offset, (float) maxY + offset, (float) maxZ + offset)
                        .color(red, green, blue, alpha)
                        .normal(normalX, normalY, normalZ)
                        .lineWidth(lineWidth);
            });
        }
    }

    @Unique
    private void renderFill(VertexConsumerProvider.Immediate bufferSource, VoxelShape shape, Matrix4f matrix, RenderSettings fillSettings) {
        float[] rgba = ColorUtils.toRgba(fillSettings.getStart());
        float red = rgba[0];
        float green = rgba[1];
        float blue = rgba[2];
        float alpha = rgba[3];
        float offset = 0.001f;

//        VertexConsumer fillConsumer = bufferSource.getBuffer(RenderTypes.debugQuads());
        RenderLayer tmp = RenderLayer.of("lightning", RenderSetup.builder(RenderPipelines.RENDERTYPE_LIGHTNING).outputTarget(OutputTarget.MAIN_TARGET).translucent().build());
        VertexConsumer fillConsumer = bufferSource.getBuffer(tmp);

//        ITEM_ENTITY_TARGET

        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float sMinX = (float) minX - offset;
            float sMinY = (float) minY - offset;
            float sMinZ = (float) minZ - offset;
            float sMaxX = (float) maxX + offset;
            float sMaxY = (float) maxY + offset;
            float sMaxZ = (float) maxZ + offset;

            // TODO: Optimize the following
            // Bottom face (Y-)
            fillConsumer.vertex(matrix, sMinX, sMinY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMinY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMinY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMinX, sMinY, sMaxZ).color(red, green, blue, alpha);

            // Top face (Y+)
            fillConsumer.vertex(matrix, sMinX, sMaxY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMinX, sMaxY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMaxY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMaxY, sMinZ).color(red, green, blue, alpha);

            // North face (Z-)
            fillConsumer.vertex(matrix, sMinX, sMinY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMinX, sMaxY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMaxY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMinY, sMinZ).color(red, green, blue, alpha);

            // South face (Z+)
            fillConsumer.vertex(matrix, sMinX, sMinY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMinY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMaxY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMinX, sMaxY, sMaxZ).color(red, green, blue, alpha);

            // West face (X-)
            fillConsumer.vertex(matrix, sMinX, sMinY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMinX, sMinY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMinX, sMaxY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMinX, sMaxY, sMinZ).color(red, green, blue, alpha);

            // East face (X+)
            fillConsumer.vertex(matrix, sMaxX, sMinY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMaxY, sMinZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMaxY, sMaxZ).color(red, green, blue, alpha);
            fillConsumer.vertex(matrix, sMaxX, sMinY, sMaxZ).color(red, green, blue, alpha);
        });
    }
}
