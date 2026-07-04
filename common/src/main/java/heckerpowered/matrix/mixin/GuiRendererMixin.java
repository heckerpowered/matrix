/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.render.PostProcessRenderer;
import heckerpowered.matrix.extension.MatrixGuiRenderState;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Supplier;

/**
 * Captures the Matrix HUD stratum into MatrixHud.hudFramebuffer, restoring 1.21's
 * "HUD drawn into its own framebuffer, then composited with blur/shadow/bloom" semantics on
 * the 26.2 deferred GUI pipeline:
 * <p>
 * prepare(): with vanilla's default fence (Integer.MAX_VALUE = no menu blur) the FIRST mesh
 * build covers every stratum; it is split at the Matrix HUD stratum boundary by temporarily
 * lowering the firstStratumAfterBlur fence, so the HUD's draws start at a recorded index in
 * their own Draw batch.
 * <p>
 * draw(): the first executeDrawRange is split at that index — vanilla GUI keeps rendering
 * into the main target, the HUD tail renders into hudFramebuffer, and
 * MatrixHud.onHudCaptured() then runs the 1.21 composite chain back onto the main target.
 */
@Mixin(GuiRenderer.class)
abstract class GuiRendererMixin {
    @Shadow
    @Final
    private GuiRenderState renderState;

    @Shadow
    @Final
    private List<?> draws;

    @Shadow
    protected abstract void addElementsToMeshes(GuiRenderState.TraverseRange range);

    @Shadow
    protected abstract void executeDrawRange(Supplier<String> name, RenderTarget target, GpuBufferSlice dynamicTransforms, int fromIndex, int toIndex);

    @Unique
    private int matrix$hudFirstDrawIndex = Integer.MAX_VALUE;

    @Unique
    private int matrix$hudDrawEndIndex = Integer.MAX_VALUE;

    private GuiRendererMixin() {
    }

    @Redirect(
            method = "prepare",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;addElementsToMeshes(Lnet/minecraft/client/renderer/state/gui/GuiRenderState$TraverseRange;)V",
                    ordinal = 0
            )
    )
    private void matrix$splitHudMeshes(GuiRenderer instance, GuiRenderState.TraverseRange range) {
        matrix$hudFirstDrawIndex = Integer.MAX_VALUE;
        matrix$hudDrawEndIndex = Integer.MAX_VALUE;
        final var matrixState = (MatrixGuiRenderState) (Object) renderState;
        if (!matrixState.hasMatrixHudStratum()) {
            addElementsToMeshes(range);
            return;
        }

        final var accessor = (GuiRenderStateAccessor) (Object) renderState;
        final var savedBlurFence = accessor.matrix$getFirstStratumAfterBlur();
        final var hudStart = matrixState.matrixHudStrataStart();
        // The captured segment is [hudStart, hudEnd); post-HUD strata (screens, tooltips,
        // toasts) begin at the end marker, or at the real blur fence if it sits lower
        // (observed in-world: a trailing vanilla stratum carries a blur marker).
        final var hudEnd = Math.min(matrixState.matrixHudStrataEnd(), savedBlurFence);
        @SuppressWarnings("unchecked") final var strata = (List<Object>) matrix$strata((GuiRenderState) (Object) renderState);

        // 1) Vanilla GUI below the HUD: fence lowered to hudStart, BEFORE_BLUR = [0, hudStart).
        accessor.matrix$setFirstStratumAfterBlur(hudStart);
        addElementsToMeshes(GuiRenderState.TraverseRange.BEFORE_BLUR);
        matrix$hudFirstDrawIndex = draws.size();

        // 2) The Matrix HUD segment [hudStart, hudEnd): AFTER_BLUR with the lowered fence
        // would run to the END of the strata list, swallowing everything extracted after the
        // HUD callback, so the tail beyond hudEnd is detached for this traversal.
        final var hudTailFrom = Math.min(hudEnd, strata.size());
        final var hudTail = new java.util.ArrayList<>(strata.subList(hudTailFrom, strata.size()));
        strata.subList(hudTailFrom, strata.size()).clear();
        addElementsToMeshes(GuiRenderState.TraverseRange.AFTER_BLUR);
        strata.addAll(hudTail);
        matrix$hudDrawEndIndex = draws.size();

        // 3) Post-HUD strata that still belong BEFORE the real blur fence [hudEnd,
        // savedBlurFence) — screens/tooltips/toasts when no menu blur is active. Vanilla's own
        // AFTER_BLUR pass only covers [savedBlurFence, end), so they are built here (into the
        // first draw range, after the captured segment) or they would never render.
        final var postTailFrom = Math.min(savedBlurFence, strata.size());
        if (hudTailFrom < postTailFrom) {
            final var postTail = new java.util.ArrayList<>(strata.subList(postTailFrom, strata.size()));
            strata.subList(postTailFrom, strata.size()).clear();
            accessor.matrix$setFirstStratumAfterBlur(hudTailFrom);
            addElementsToMeshes(GuiRenderState.TraverseRange.AFTER_BLUR);
            strata.addAll(postTail);
        }
        accessor.matrix$setFirstStratumAfterBlur(savedBlurFence);
    }

    @Redirect(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;II)V",
                    ordinal = 0
            )
    )
    private void matrix$drawHudRange(GuiRenderer instance, Supplier<String> name, RenderTarget target, GpuBufferSlice dynamicTransforms, int fromIndex, int toIndex) {
        final var hudFrom = Math.min(matrix$hudFirstDrawIndex, toIndex);
        final var hudTo = Math.min(matrix$hudDrawEndIndex, toIndex);
        if (fromIndex < hudFrom) {
            executeDrawRange(name, target, dynamicTransforms, fromIndex, hudFrom);
        }
        if (hudFrom < hudTo) {
            final var hudFramebuffer = MatrixHud.INSTANCE.getHudFramebuffer();
            PostProcessRenderer.clear(hudFramebuffer);
            MatrixHud.onHudCaptureBegin();
            executeDrawRange(name, hudFramebuffer, dynamicTransforms, hudFrom, hudTo);
            MatrixHud.onHudCaptured();
        }
        if (hudTo < toIndex) {
            // Post-HUD GUI (screens, tooltips, toasts) drawn after the composite, back onto
            // the main target — the 1.21 ordering, where they rendered after InGameHud.
            executeDrawRange(name, target, dynamicTransforms, hudTo, toIndex);
        }
    }

    @Unique
    private static List<?> matrix$strata(GuiRenderState state) {
        return ((GuiRenderStateAccessor) (Object) state).matrix$getStrata();
    }
}
