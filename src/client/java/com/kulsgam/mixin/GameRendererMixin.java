package com.kulsgam.mixin;

import com.kulsgam.BlockOverlayClient;
import com.kulsgam.config.BlockOverlayConfig;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "shouldRenderBlockOutline", at = @At("RETURN"), cancellable = true)
    private void shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        BlockOverlayClient client = BlockOverlayClient.instance;
        if (client == null) {
            return;
        }
        BlockOverlayConfig config = client.getConfig();
        if (config != null && config.persistence) {
            cir.setReturnValue(true);
        }
    }
}
