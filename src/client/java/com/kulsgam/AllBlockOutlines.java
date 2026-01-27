package com.kulsgam;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AllBlockOutlines {
    public static void onAfterEntities(WorldRenderContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return;

        MatrixStack matrices = ctx.matrices();

        Vec3d camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        BlockPos center = BlockPos.ofFloored(camPos);

        VertexConsumerProvider vcp = ctx.consumers();

        // If consumers() is null (it is @Nullable), fall back to an Immediate provider.

        VertexConsumer vc = vcp.getBuffer(RenderLayers.lines());

        int radius = 8;

        for (BlockPos pos : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (world.isAir(pos)) continue;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (!world.isAir(neighbor)) continue; // only draw edges of exposed faces
                drawFaceEdges(vc, matrices, pos, dir, camPos);
            }
        }
    }

    private static void drawFaceEdges(
            VertexConsumer vc,
            MatrixStack matrices,
            BlockPos pos,
            Direction face,
            Vec3d camPos
    ) {
        Vec3d base = Vec3d.of(pos).subtract(camPos);

        double x = base.x;
        double y = base.y;
        double z = base.z;

        switch (face) {

            case NORTH -> { // z
                line(vc, matrices, x, y, z, x + 1, y, z);
                line(vc, matrices, x + 1, y, z, x + 1, y + 1, z);
                line(vc, matrices, x + 1, y + 1, z, x, y + 1, z);
                line(vc, matrices, x, y + 1, z, x, y, z);
            }

            case SOUTH -> { // z + 1
                line(vc, matrices, x, y, z + 1, x + 1, y, z + 1);
                line(vc, matrices, x + 1, y, z + 1, x + 1, y + 1, z + 1);
                line(vc, matrices, x + 1, y + 1, z + 1, x, y + 1, z + 1);
                line(vc, matrices, x, y + 1, z + 1, x, y, z + 1);
            }

            case WEST -> { // x
                line(vc, matrices, x, y, z, x, y, z + 1);
                line(vc, matrices, x, y, z + 1, x, y + 1, z + 1);
                line(vc, matrices, x, y + 1, z + 1, x, y + 1, z);
                line(vc, matrices, x, y + 1, z, x, y, z);
            }

            case EAST -> { // x + 1
                line(vc, matrices, x + 1, y, z, x + 1, y, z + 1);
                line(vc, matrices, x + 1, y, z + 1, x + 1, y + 1, z + 1);
                line(vc, matrices, x + 1, y + 1, z + 1, x + 1, y + 1, z);
                line(vc, matrices, x + 1, y + 1, z, x + 1, y, z);
            }

            case DOWN -> { // y
                line(vc, matrices, x, y, z, x + 1, y, z);
                line(vc, matrices, x + 1, y, z, x + 1, y, z + 1);
                line(vc, matrices, x + 1, y, z + 1, x, y, z + 1);
                line(vc, matrices, x, y, z + 1, x, y, z);
            }

            case UP -> { // y + 1
                line(vc, matrices, x, y + 1, z, x + 1, y + 1, z);
                line(vc, matrices, x + 1, y + 1, z, x + 1, y + 1, z + 1);
                line(vc, matrices, x + 1, y + 1, z + 1, x, y + 1, z + 1);
                line(vc, matrices, x, y + 1, z + 1, x, y + 1, z);
            }
        }
    }

    private static void line(
            VertexConsumer vc,
            MatrixStack matrices,
            double x1, double y1, double z1,
            double x2, double y2, double z2
    ) {
        MatrixStack.Entry entry = matrices.peek();

        vc.vertex(entry.getPositionMatrix(), (float) x1, (float) y1, (float) z1)
                .color(255, 255, 255, 255)
                .normal(entry, 0, 1, 0).lineWidth(10);

        vc.vertex(entry.getPositionMatrix(), (float) x2, (float) y2, (float) z2)
                .color(255, 255, 255, 255)
                .normal(entry, 0, 1, 0).lineWidth(10);
    }
}
