package com.kulsgam.listeners;

import com.kulsgam.config.BlockOverlayConfig;
import com.kulsgam.config.RenderSettings;
import com.kulsgam.gui.BlockOverlayScreen;
import com.kulsgam.utils.Animator;
import com.kulsgam.utils.ColorUtils;
import com.kulsgam.utils.RenderUtils;
import com.kulsgam.utils.enums.RenderMode;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.util.Set;

public class BlockOverlayListener {
    private final MinecraftClient client;
    private final BlockOverlayConfig config;
    private final Logger logger;
    private final Animator blockAnimator = new Animator(350.0);
    private final Set<Block> plantBlocks = Set.of(
            Blocks.SHORT_GRASS,
            Blocks.TALL_GRASS,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.DANDELION,
            Blocks.POPPY,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.SUNFLOWER,
            Blocks.LILAC,
            Blocks.ROSE_BUSH,
            Blocks.PEONY
    );
    private boolean blockShrinking;

    public BlockOverlayListener(MinecraftClient client, BlockOverlayConfig config, Logger logger) {
        this.client = client;
        this.config = config;
        this.logger = logger;
    }

    public boolean handleBlockOutline(WorldRenderContext context, OutlineRenderState outlineRenderState) {
        RenderMode renderMode = config.renderMode;
        return renderMode == RenderMode.VANILLA;
    }

    public void renderWorld(WorldRenderContext context) {
        Entity entity = client.getCameraEntity();
        if (entity == null) {
            return;
        }
        if (client.currentScreen instanceof BlockOverlayScreen) {
            float tickDelta = client.getRenderTickCounter().getTickProgress(false);
            renderPreview(context, entity, tickDelta);
            return;
        }
        if (client.options.hudHidden) {
            return;
        }
        RenderMode renderMode = config.renderMode;
        if (renderMode != RenderMode.SIDE && renderMode != RenderMode.FULL) {
            return;
        }
        BlockState blockState = getFocusedBlock();
        if (blockState == null) {
            return;
        }

        float tickDelta = client.getRenderTickCounter().getTickProgress(false);
        renderBlockOverlay(context, blockState, entity, tickDelta);
    }

    private void renderBlockOverlay(WorldRenderContext context, BlockState blockState, Entity entity, float tickDelta) {
        RenderSettings overlaySettings = config.fillRender;
        RenderSettings outlineSettings = config.outlineRender;

        int overlayStartColor = overlaySettings.getStart();
        int overlayEndColor = overlaySettings.getEnd();
        boolean overlay = overlaySettings.visible;
        boolean outline = outlineSettings.visible;
        int outlineStartColor = outlineSettings.getStart();
        int outlineEndColor = outlineSettings.getEnd();

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit)) return;
        if (client.world == null) return;

        BlockPos blockPos = blockHit.getBlockPos();
        Direction side = config.renderMode == RenderMode.SIDE ? blockHit.getSide() : null;

        VoxelShape shape = blockState.getOutlineShape(client.world, blockPos);
        if (shape.isEmpty()) return;

        // 1.21.11 accessors
        Vec3d camera = context.worldState().cameraRenderState.pos;
        MatrixStack matrices = context.matrices();

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        VertexConsumerProvider consumers = context.consumers();

        // Filled overlay layer (exists in Yarn 1.21.11)
        VertexConsumer fillConsumer = consumers.getBuffer(RenderLayers.debugFilledBox());

        for (Box box : shape.getBoundingBoxes()) {
            Box worldBox = box.offset(blockPos).expand(0.002);

            // ---- FILL (your overlay) ----
            if (overlay) {
                // If you still want side-only fill, you must implement that in your emitter.
                // For now: fill whole box.
                // TODO: fill side only
                RenderUtils.emitBoxQuads(matrices, fillConsumer, worldBox, side, overlayStartColor, overlayEndColor);
            }

            // ---- OUTLINE ----
            if (outline) {
                VertexConsumer lineConsumer = consumers.getBuffer(RenderLayers.lines());
                Box outlineBox = worldBox.expand(0.001);
                drawOutline(matrices, lineConsumer, outlineBox, side, outlineStartColor, outlineEndColor, (float) config.thickness);
            }
        }

        matrices.pop();
    }

    private void renderPreview(WorldRenderContext context, Entity entity, float tickDelta) {
        Vec3d look = entity.getRotationVec(tickDelta).multiply(2.0);
        long time = System.currentTimeMillis();
        double rotation = (time / 20.0 + tickDelta) % 360.0;
        double height = MathHelper.sin((float) ((time / 20.0 % 157.0) + tickDelta) / 25.0f) * 0.2;

        Perspective perspective = client.options.getPerspective();
        double distance = perspective == Perspective.THIRD_PERSON_FRONT ? 0.5 : (perspective == Perspective.THIRD_PERSON_BACK ? -0.5 : 1.5);
        distance = blockAnimator.getValue(distance, Math.abs(distance * 20.0), blockShrinking, false);

        BlockState blockState = Blocks.STONE.getDefaultState();
        Direction side = entity.getPitch() >= 50.0f ? Direction.UP : (entity.getPitch() >= -50.0f ? Direction.NORTH : Direction.DOWN);

        // New camera/matrices access
        Vec3d camera = context.worldState().cameraRenderState.pos; // :contentReference[oaicite:8]{index=8}
        MatrixStack matrices = context.matrices();                // :contentReference[oaicite:9]{index=9}

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        matrices.translate(look.x * distance, look.y * distance + 1.11 + height, look.z * distance);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees((float) rotation));
        matrices.translate(-0.5, 0.0, -0.5);

        BlockRenderManager blockRenderManager = client.getBlockRenderManager();

        // Use your own immediate consumers that you are allowed to flush
        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();

        blockRenderManager.renderBlockAsEntity(blockState, matrices, consumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        consumers.draw(); // this now resolves correctly

        // For the outline/overlay preview box: same advice as above — write vertices into layers
        // instead of RenderSystem state flipping.

        matrices.pop();
    }

    private BlockState getFocusedBlock() {
        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHit)) {
            return null;
        }
        BlockPos blockPos = blockHit.getBlockPos();
        if (client.world == null) {
            return null;
        }
        BlockState blockState = client.world.getBlockState(blockPos);
        if (blockState.isAir()) {
            return null;
        }
        Block block = blockState.getBlock();
        if (config.hidePlants && (plantBlocks.contains(block) || blockState.isIn(BlockTags.FLOWERS))) {
            return null;
        }
        if (!config.barriers && block == Blocks.BARRIER) {
            return null;
        }
        return blockState;
    }

    public void resetAnimation(boolean blockShrinking) {
        this.blockShrinking = blockShrinking;
        blockAnimator.reset();
    }

    private void drawOutline(MatrixStack matrices, VertexConsumer lineConsumer, Box box, Direction side,
                             int startColor, int endColor, float lineWidth) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        double minY = box.minY;
        double maxY = box.maxY;
        int passes = Math.max(1, (int) lineWidth);
        float offsetIncrement = 0.001f;

        for (int pass = 0; pass < passes; pass++) {
            float offset = pass * offsetIncrement;
            if (side == null) {
                drawBoxEdges(lineConsumer, matrix, box, startColor, endColor, minY, maxY, lineWidth, offset);
            } else {
                drawFaceEdges(lineConsumer, matrix, box, side, startColor, endColor, minY, maxY, lineWidth, offset);
            }
        }
    }

    private void drawBoxEdges(VertexConsumer lineConsumer, Matrix4f matrix, Box box, int startColor, int endColor,
                              double minY, double maxY, float lineWidth, float offset) {
        float minX = (float) box.minX + offset;
        float minYBox = (float) box.minY + offset;
        float minZ = (float) box.minZ + offset;
        float maxX = (float) box.maxX + offset;
        float maxYBox = (float) box.maxY + offset;
        float maxZ = (float) box.maxZ + offset;

        // bottom
        emitLine(lineConsumer, matrix, minX, minYBox, minZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                maxX, minYBox, minZ, colorForY(startColor, endColor, minYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, maxX, minYBox, minZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                maxX, minYBox, maxZ, colorForY(startColor, endColor, minYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, maxX, minYBox, maxZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                minX, minYBox, maxZ, colorForY(startColor, endColor, minYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, minX, minYBox, maxZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                minX, minYBox, minZ, colorForY(startColor, endColor, minYBox, minY, maxY), lineWidth);

        // top
        emitLine(lineConsumer, matrix, minX, maxYBox, minZ, colorForY(startColor, endColor, maxYBox, minY, maxY),
                maxX, maxYBox, minZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, maxX, maxYBox, minZ, colorForY(startColor, endColor, maxYBox, minY, maxY),
                maxX, maxYBox, maxZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, maxX, maxYBox, maxZ, colorForY(startColor, endColor, maxYBox, minY, maxY),
                minX, maxYBox, maxZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, minX, maxYBox, maxZ, colorForY(startColor, endColor, maxYBox, minY, maxY),
                minX, maxYBox, minZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);

        // vertical
        emitLine(lineConsumer, matrix, minX, minYBox, minZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                minX, maxYBox, minZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, maxX, minYBox, minZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                maxX, maxYBox, minZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, maxX, minYBox, maxZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                maxX, maxYBox, maxZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);
        emitLine(lineConsumer, matrix, minX, minYBox, maxZ, colorForY(startColor, endColor, minYBox, minY, maxY),
                minX, maxYBox, maxZ, colorForY(startColor, endColor, maxYBox, minY, maxY), lineWidth);
    }

    private void drawFaceEdges(VertexConsumer lineConsumer, Matrix4f matrix, Box box, Direction side,
                               int startColor, int endColor, double minY, double maxY, float lineWidth, float offset) {
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
            int startEdgeColor = colorForY(startColor, endColor, start[1], minY, maxY);
            int endEdgeColor = colorForY(startColor, endColor, end[1], minY, maxY);
            emitLine(lineConsumer, matrix, start[0], start[1], start[2], startEdgeColor,
                    end[0], end[1], end[2], endEdgeColor, lineWidth);
        }
    }

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

    private int colorForY(int startColor, int endColor, double y, double minY, double maxY) {
        double range = Math.max(1e-6, maxY - minY);
        float t = (float) ((y - minY) / range);
        return lerpColor(startColor, endColor, t);
    }

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
