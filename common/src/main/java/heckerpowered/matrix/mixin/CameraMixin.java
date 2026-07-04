/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.TimeController;
import heckerpowered.matrix.client.render.MatrixRenderSystem;
import heckerpowered.matrix.client.render.post.CameraShake;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2: fov calculation and the projection matrix moved from GameRenderer into Camera, so
 * the pre-migration GameRenderer.getFov / loadProjectionMatrix / camera-shake hooks live
 * here now:
 * <ul>
 * <li>calculateFov/calculateHudFov × MatrixHud.fovAnimation — the former getFov TAIL hook.</li>
 * <li>getCameraEntityPartialTicks — the former standalone-render-tick delta override.</li>
 * <li>extractRenderState TAIL — captures view/projection for MatrixRenderSystem (former
 *     loadProjectionMatrix redirect) and applies CameraShake to the extracted view rotation
 *     (former renderWorld view-matrix perturbation before frustum setup).</li>
 * </ul>
 */
@Mixin(Camera.class)
class CameraMixin {
    private CameraMixin() {
    }

    @Inject(method = "getCameraEntityPartialTicks", at = @At("RETURN"), cancellable = true)
    private void getCameraEntityPartialTicks(DeltaTracker deltaTracker, CallbackInfoReturnable<Float> cir) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            cir.setReturnValue(TimeController.standaloneRenderTickCounter.getGameTimeDeltaPartialTick(true));
        }
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void calculateFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * (float) MatrixHud.fovAnimation.getAnimatedValue());
    }

    @Inject(method = "calculateHudFov", at = @At("RETURN"), cancellable = true)
    private void calculateHudFov(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * (float) MatrixHud.fovAnimation.getAnimatedValue());
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void extractRenderState(CameraRenderState renderState, float partialTick, CallbackInfo ci) {
        CameraShake.applyCameraShake(renderState.viewRotationMatrix);
        MatrixRenderSystem.setupMatrix((Camera) (Object) this, renderState.projectionMatrix);
    }
}
