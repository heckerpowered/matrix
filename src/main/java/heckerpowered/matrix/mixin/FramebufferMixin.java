package heckerpowered.matrix.mixin;

import heckerpowered.matrix.core.FramebufferExtension;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL46.*;

@Mixin(Framebuffer.class)
class FramebufferMixin implements FramebufferExtension {

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

    @Inject(method = "initFbo", at = @At("TAIL"))
    private void initFbo(int width, int height, boolean getError, CallbackInfo ci) {
        if (useMipmaps) {
            glBindTexture(GL_TEXTURE_2D, colorAttachment);
            glGenerateMipmap(GL_TEXTURE_2D);
        }
    }
}
