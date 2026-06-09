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

@Mixin(LevelRenderer.class)
class LevelRendererStandaloneMixin {
    private LevelRendererStandaloneMixin() {
    }

    @Inject(
            method = "lambda$addMainPass$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void matrix$beginRenderEntityEffects(
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
    private void matrix$endRenderEntityEffects(
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
