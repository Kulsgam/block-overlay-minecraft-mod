package com.kulsgam;

import com.kulsgam.mixin.accessor.GameRendererInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public final class PostEffectController {
    private static final Identifier BLOCK_OVERLAY_EFFECT = Identifier.of("block-overlay", "all_block_outlines");

    private PostEffectController() {
    }

    public static void enableAllBlockOutlineEffect() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.gameRenderer == null) return;

        ((GameRendererInvoker) client.gameRenderer).callSetPostProcessor(BLOCK_OVERLAY_EFFECT);
        // GameRenderer internally tracks enabled state; if you need hard-enable,
        // you can add an @Accessor for postProcessorEnabled and set it true.
    }

    public static void disableBlockOverlayEffect() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.gameRenderer == null) return;

        client.gameRenderer.clearPostProcessor();
    }
}
