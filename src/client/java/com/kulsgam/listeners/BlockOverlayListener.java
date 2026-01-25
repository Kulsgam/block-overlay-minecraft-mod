package com.kulsgam.listeners;

import com.kulsgam.config.BlockOverlayConfig;
import com.kulsgam.config.RenderSettings;
import com.kulsgam.gui.BlockOverlayScreen;
import com.kulsgam.utils.Animator;
import com.kulsgam.utils.RenderUtils;
import com.kulsgam.utils.enums.RenderMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.client.render.state.OutlineRenderState;
//import com.mojang.blaze3d.opengl.GlStateManager;
import org.lwjgl.opengl.GL14;
import org.slf4j.Logger;

import java.util.Set;

public class BlockOverlayListener {
    private final MinecraftClient client;
    private final BlockOverlayConfig config;
    private final ShadersListener shadersListener;
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

    public BlockOverlayListener(MinecraftClient client, BlockOverlayConfig config, ShadersListener shadersListener, Logger logger) {
        this.client = client;
        this.config = config;
        this.shadersListener = shadersListener;
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

    public void tick(MinecraftClient client) {
        shadersListener.tick();
    }

    private void renderBlockOverlay(WorldRenderContext context, BlockState blockState, Entity entity, float tickDelta) {
        RenderSettings overlaySettings = config.overlayRender;
        RenderSettings outlineSettings = config.outlineRender;

        int overlayStartColor = overlaySettings.getStart();
        int overlayEndColor = overlaySettings.getEnd();
        int outlineStartColor = outlineSettings.getStart();
        int outlineEndColor = outlineSettings.getEnd();

        boolean overlay = overlaySettings.visible;
        boolean outline = outlineSettings.visible;
        int outlineColor = outlineSettings.getStart();

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

                // Outline the whole voxel shape, offset to the block position in world space.
                // No need to translate matrices for blockPos; we pass offsets directly.
                VertexRendering.drawOutline(
                        matrices,
                        lineConsumer,
                        shape,
                        blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                        outlineColor,
                        (float) config.thickness
                );
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

    private boolean canRenderBlockOverlay() {
        Entity entity = client.getCameraEntity();
        if (!(entity instanceof PlayerEntity player)) {
            return true;
        }
        if (player.getAbilities().creativeMode) {
            return true;
        }
        ItemStack heldItem = player.getMainHandStack();
        if (!(client.crosshairTarget instanceof BlockHitResult blockHit)) {
            return false;
        }
        BlockPos blockPos = blockHit.getBlockPos();
        boolean canPlace = heldItem.getItem() instanceof BlockItem; // TODO: Maybe remove this if issues occur

        if (client.world != null) {
            BlockState blockState = client.world.getBlockState(blockPos);
            boolean canInteract = blockState.hasBlockEntity();
        } else {
            logger.error("client.world is null");
        }

        return canPlace;
    }

    public void resetAnimation(boolean blockShrinking) {
        this.blockShrinking = blockShrinking;
        blockAnimator.reset();
    }
}
