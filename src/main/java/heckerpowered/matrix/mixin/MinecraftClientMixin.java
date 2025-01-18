package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.core.FramebufferSpoof;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
class MinecraftClientMixin {
    private MinecraftClientMixin() {
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void doAttack(CallbackInfoReturnable<Boolean> cir) {
        MatrixHud.onDoAttack();
    }

    @Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
    private void getFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
        final var spoofedFramebuffer = FramebufferSpoof.getSpoofedFramebuffer();
        if (spoofedFramebuffer != null) {
            cir.setReturnValue(spoofedFramebuffer);
        }
    }
}
