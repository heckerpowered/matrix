/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import heckerpowered.matrix.client.event.InitAttachmentCallback;
import heckerpowered.matrix.client.render.RenderExtensionsKt;
import heckerpowered.matrix.core.FramebufferExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

/**
 * Mixin for the {@link RenderTarget} class to extend its functionality.
 * <p>
 * Adds optional full-mipmap-chain allocation for the color attachment (used by the bloom
 * pipeline), applied through the backend-agnostic {@link GpuDevice#createTexture} wrapper
 * inside {@code createBuffers}, so it works on both the Vulkan and OpenGL backends.
 * <p>
 * 26.2 release note: the pre-migration global HDR color-format override is gone. Release
 * pipelines declare their color target format and render passes validate the attachment
 * against it, so vanilla-rendered targets (the main framebuffer, entity/GUI capture targets)
 * must keep the format vanilla created them with; HDR formats are now passed explicitly to
 * the mod's own render targets instead (see FramebufferExtension.framebufferColorFormat).
 *
 * @author heckerpowered
 */
@Mixin(RenderTarget.class)
class FramebufferMixin implements FramebufferExtension {
    @Unique
    private boolean useMipmaps = false;

    @SuppressWarnings("all")
    @Override
    public boolean getUseMipmaps() {
        return useMipmaps;
    }

    @SuppressWarnings("all")
    @Override
    public void setUseMipmaps(boolean b) {
        useMipmaps = b;
    }

    /**
     * The second createTexture invocation inside createBuffers allocates the color
     * attachment (the first one is the depth attachment); allocate a full mipmap chain
     * when enabled.
     */
    @Redirect(
            method = "createBuffers",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/GpuDevice;createTexture(Ljava/util/function/Supplier;ILcom/mojang/blaze3d/GpuFormat;IIII)Lcom/mojang/blaze3d/textures/GpuTexture;",
                    ordinal = 1
            )
    )
    private GpuTexture matrix$createColorTexture(GpuDevice device, Supplier<String> label, int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        final var levels = useMipmaps ? Math.max(RenderExtensionsKt.recommendMipLevel(width, height), 1) : mipLevels;
        final var texture = device.createTexture(label, usage, format, width, height, depthOrLayers, levels);
        InitAttachmentCallback.EVENT.invoker().onInitAttachment((RenderTarget) (Object) this);
        return texture;
    }
}
