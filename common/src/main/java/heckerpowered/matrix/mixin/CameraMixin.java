/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.TimeController;
import heckerpowered.matrix.client.render.MatrixRenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
class CameraMixin {
    private CameraMixin() {
    }

    @Inject(method = "getCameraEntityPartialTicks", at = @At("RETURN"), cancellable = true)
    private void getCameraEntityPartialTicks(DeltaTracker deltaTracker, CallbackInfoReturnable<Float> cir) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            cir.setReturnValue(TimeController.standaloneRenderTickCounter.getTickDelta(true));
        }
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void matrix$calculateFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * (float) MatrixHud.fovAnimation.getAnimatedValue());
    }

    @Inject(method = "calculateHudFov", at = @At("RETURN"), cancellable = true)
    private void matrix$calculateHudFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * (float) MatrixHud.fovAnimation.getAnimatedValue());
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void matrix$extractRenderState(CameraRenderState renderState, float partialTick, CallbackInfo ci) {
        MatrixRenderSystem.setupMatrix((Camera) (Object) this, renderState.projectionMatrix);
    }
}
