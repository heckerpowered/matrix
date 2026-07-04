/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import heckerpowered.matrix.client.TimeController;
import heckerpowered.matrix.client.core.FramebufferSpoof;
import heckerpowered.matrix.client.render.GuideLineRenderer;
import heckerpowered.matrix.client.render.PostProcessRenderer;
import heckerpowered.matrix.client.shader.BlurRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

/**
 * 26.2 re-anchoring of the pre-migration GameRenderer hooks:
 * onResized→resize, render-before-InGameHud→render-before-GuiRenderer.render(),
 * renderWorld's Camera.update(BlockView,Entity,ZZF)→update's Camera.update(DeltaTracker)
 * (the standalone tick counter is itself a DeltaTracker now), renderHand→renderItemInHand,
 * and the getFov/loadProjectionMatrix hooks moved to {@link CameraMixin} where fov and the
 * projection matrix now live. The Minecraft.getFramebuffer() spoof retargets the
 * mainRenderTarget() accessor.
 */
@Mixin(GameRenderer.class)
class GameRendererMixin {
    private GameRendererMixin() {
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo ci) {
        // UIBlurShader.setupDimensions(width, height);
        BlurRenderer.onResize(width, height);
        PostProcessRenderer.onResize(width, height);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V", shift = At.Shift.BEFORE))
    private void beginRender(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        // The 1.21 anchor (before InGameHud.render) only fired with a level loaded;
        // GuiRenderer.render() also covers the loading overlay and title screen, so guard
        // to preserve the original "in-world only" semantics (and to avoid touching the
        // shader pipelines before the first resource reload has loaded their sources).
        if (Minecraft.getInstance().level == null) {
            return;
        }
        PostProcessRenderer.renderToMinecraftFramebuffer();
        // Builds this frame's guide-line mesh and draws the 8x HDR bloom-feed overlay; the
        // VISIBLE lines join the HUD capture later in the frame (MatrixHud.onHudCaptureBegin),
        // matching 1.21 where they were hudFramebuffer content over the backdrop blur.
        GuideLineRenderer.render();
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void endRender(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        // PostProcessRenderer.endHudRender();
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;update(Lnet/minecraft/client/DeltaTracker;)V"))
    private void updateCamera(Camera instance, DeltaTracker deltaTracker) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            instance.update(TimeController.standaloneRenderTickCounter);
        } else {
            instance.update(deltaTracker);
        }
    }

    @ModifyVariable(method = "renderItemInHand", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float renderHand(float tickDelta) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            return TimeController.standaloneRenderTickCounter.getGameTimeDeltaPartialTick(true);
        }
        return tickDelta;
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bobView(CameraRenderState cameraRenderState, PoseStack matrices, CallbackInfo ci) {
        final var minecraft = Minecraft.getInstance();
        final var player = minecraft.player;
        if (player != null && isPhaseWalking(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "mainRenderTarget", at = @At("HEAD"), cancellable = true)
    private void getFramebuffer(CallbackInfoReturnable<RenderTarget> cir) {
        final var spoofedFramebuffer = FramebufferSpoof.getSpoofedFramebuffer();
        if (spoofedFramebuffer != null) {
            cir.setReturnValue(spoofedFramebuffer);
        }
    }
}
