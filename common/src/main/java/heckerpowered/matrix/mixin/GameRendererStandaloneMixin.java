/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.render.PostProcessRenderer;
import heckerpowered.matrix.client.shader.BlurRenderer;
import heckerpowered.matrix.client.TimeController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
class GameRendererStandaloneMixin {
    private GameRendererStandaloneMixin() {
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void matrix$onResize(int width, int height, CallbackInfo ci) {
        BlurRenderer.onResize(width, height);
        PostProcessRenderer.onResize(width, height);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void matrix$beforeGuiRender(DeltaTracker deltaTracker, boolean tick, CallbackInfo ci) {
        PostProcessRenderer.renderToMinecraftFramebuffer();
    }

    @ModifyVariable(method = "renderItemInHand", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float useStandaloneHandTickDelta(float tickDelta) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            return TimeController.standaloneRenderTickCounter.getTickDelta(true);
        }
        return tickDelta;
    }
}
