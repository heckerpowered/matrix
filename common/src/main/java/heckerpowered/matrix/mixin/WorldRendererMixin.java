/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import heckerpowered.matrix.client.render.ScreenEffectRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher.PreparedFrame;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2: {@code WorldRenderer} (Yarn) / {@code LevelRenderer} (Mojang) no longer renders
 * entities through a single linear {@code render} method with a shared immediate
 * {@link net.minecraft.client.renderer.MultiBufferSource.BufferSource}. Entity/terrain
 * submission now happens inside the frame-graph pass built by {@code addMainPass}, whose
 * body is compiled into the synthetic lambda {@code lambda$addMainPass$0}. Solid entities are
 * drawn during {@link PreparedFrame#executeSolid()} and translucent-after-terrain entities
 * during {@link PreparedFrame#executeTranslucentAfterTerrain()}, so the begin/end hooks are
 * re-anchored immediately before/after those calls to preserve the original bracketing of
 * {@link ScreenEffectRenderer#beginRenderEntity()}/{@link ScreenEffectRenderer#endRenderEntity()}
 * around entity rendering.
 */
@Mixin(LevelRenderer.class)
class WorldRendererMixin {
    private WorldRendererMixin() {
    }

    @Inject(
            method = "lambda$addMainPass$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beginRenderEntity(
            GpuBufferSlice gpuBufferSlice,
            LevelRenderState levelRenderState,
            ProfilerFiller profiler,
            ChunkSectionsToRender sectionsToRender,
            ResourceHandle resourceHandle,
            PreparedFrame preparedFrame,
            ResourceHandle resourceHandle2,
            ResourceHandle resourceHandle3,
            ResourceHandle resourceHandle4,
            ResourceHandle resourceHandle5,
            CallbackInfo ci
    ) {
        ScreenEffectRenderer.beginRenderEntity();
    }

    @Inject(
            method = "lambda$addMainPass$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucentAfterTerrain()V",
                    shift = At.Shift.AFTER
            )
    )
    private void endRenderEntity(
            GpuBufferSlice gpuBufferSlice,
            LevelRenderState levelRenderState,
            ProfilerFiller profiler,
            ChunkSectionsToRender sectionsToRender,
            ResourceHandle resourceHandle,
            PreparedFrame preparedFrame,
            ResourceHandle resourceHandle2,
            ResourceHandle resourceHandle3,
            ResourceHandle resourceHandle4,
            ResourceHandle resourceHandle5,
            CallbackInfo ci
    ) {
        ScreenEffectRenderer.endRenderEntity();
    }
}
