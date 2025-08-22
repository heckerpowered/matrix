/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.event.InitAttachmentCallback;
import heckerpowered.matrix.client.render.OpenGLExtensions;
import heckerpowered.matrix.client.render.RenderExtensionsKt;
import heckerpowered.matrix.core.FramebufferExtension;
import kotlin.Unit;
import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.IntBuffer;

import static com.mojang.blaze3d.platform.GlStateManager._texImage2D;
import static heckerpowered.matrix.Matrix.LOGGER;
import static org.lwjgl.opengl.GL46.*;

/**
 * Mixin for the {@link Framebuffer} class to extend its functionality.
 * <p>
 * This mixin adds support for:
 * <ul>
 * <li>Using alternative color formats for the framebuffer texture, such as RGBA16, enabling HDR rendering.</li>
 * <li>Allocating and generating mipmaps for framebuffer's color attachment texture during initialization, if enabled.</li>
 * </ul>
 * </p>
 *
 * @author heckerpowered
 */
@Mixin(Framebuffer.class)
class FramebufferMixin implements FramebufferExtension {
    @Unique
    private static final Marker MARKER = MarkerFactory.getMarker("FRAMEBUFFER_MIXIN");
    @Shadow
    protected int colorAttachment;
    @Unique
    private boolean useMipmaps = false;

    @ModifyConstant(method = "initFbo", constant = @Constant(intValue = GL_RGBA8))
    private int modify$imageFormat(int constant) {
        return FramebufferExtension.getFramebufferColorFormat();
    }

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

    @Redirect(
            method = "initFbo",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"
            )
    )
    private void texImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, @Nullable IntBuffer pixels) {
        InitAttachmentCallback.EVENT.invoker().onInitAttachment((Framebuffer) (Object) this);
        final var isDepthAttachment = format == GL_DEPTH_COMPONENT;
        if (!useMipmaps || isDepthAttachment) {
            _texImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
            return;
        }

        OpenGLExtensions.clearGLError();

        final var self = (Framebuffer) (Object) this;
        final var recommendMipLevels = RenderExtensionsKt.recommendMipLevel(self);
        glTexStorage2D(target, recommendMipLevels, internalFormat, width, height);
        OpenGLExtensions.checkGLError(error -> {
            final var name = OpenGLExtensions.getErrorName(error);
            final var message = OpenGLExtensions.getErrorDescription(error);
            LOGGER.error(MARKER, "Error occurs during call `glTexStorage2D`: {}", name);
            LOGGER.error(MARKER, message);
            LOGGER.error(MARKER, "Target: {}, Level: {}, InternalFormat: {}, Width: {}, Height: {}", target, level, internalFormat, width, height);
            return Unit.INSTANCE;
        });

        final var packedPixelDataType = OpenGLExtensions.getPackedPixelDataTypeForFormat(internalFormat);
        final var bytesPerPixel = OpenGLExtensions.getBytesPerPixel(format, type);
        final var bufferSize = width * height * bytesPerPixel;
        final var buffer = MemoryUtil.memAlloc(bufferSize);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, format, type, buffer);
        MemoryUtil.memFree(buffer);

        OpenGLExtensions.checkGLError(error -> {
            final var name = OpenGLExtensions.getErrorName(error);
            final var message = OpenGLExtensions.getErrorDescription(error);
            LOGGER.error(MARKER, "Error occurs during call `glTexSubImage2D`: {}", name);
            LOGGER.error(MARKER, message);
            LOGGER.error(MARKER, "Width: {}, Height: {}, Format: {} Type: {}:", width, height, format, packedPixelDataType);
            return Unit.INSTANCE;
        });
    }
}
