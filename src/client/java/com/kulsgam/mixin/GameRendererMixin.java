package com.kulsgam.mixin;

import com.kulsgam.BlockOverlayClient;
import com.kulsgam.PostEffectController;
import com.kulsgam.config.BlockOverlayConfig;
import com.kulsgam.utils.enums.RenderMode;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    private boolean allBlockOutlinesApplied = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void allBlockOutlinesApplyOnce(RenderTickCounter renderTickCounter, boolean tick, CallbackInfo ci) {
        if (!allBlockOutlinesApplied) {
            PostEffectController.enableAllBlockOutlineEffect();
            allBlockOutlinesApplied = true;
        }
    }

    @Inject(method = "clearPostProcessor", at = @At("TAIL"))
    private void allBlockOutlinesResetFlag(CallbackInfo ci) {
        allBlockOutlinesApplied = false;
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("RETURN"), cancellable = true)
    private void shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        BlockOverlayClient client = BlockOverlayClient.instance;
        if (client == null) {
            return;
        }
        BlockOverlayConfig config = client.getConfig();
        if (config != null) {
            RenderMode outlineMode = config.outlineRender.renderMode;
            RenderMode fillMode = config.fillRender.renderMode;
            boolean fillCustom = fillMode == RenderMode.FULL || fillMode == RenderMode.SIDE;
            if (outlineMode == RenderMode.HIDDEN && !fillCustom) {
                cir.setReturnValue(false);
                return;
            }
            if (config.persistence) {
                cir.setReturnValue(true);
            }
        }
    }
}
