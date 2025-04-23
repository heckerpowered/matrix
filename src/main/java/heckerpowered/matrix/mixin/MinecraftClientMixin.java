package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.client.TimeController;
import heckerpowered.matrix.client.core.FramebufferSpoof;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
    private MinecraftClientMixin() {
    }

    @Shadow
    public abstract void tick();

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

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;draw(II)V", shift = At.Shift.AFTER))
    private void onFinishedRender(boolean tick, CallbackInfo ci) {
        // PostProcessRenderer.clearPostProcessFramebuffer();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;beginRenderTick(JZ)I"))
    private int beginRenderTick(RenderTickCounter.Dynamic instance, long timeMillis, boolean tick) {
        final var tickCount = instance.beginRenderTick(timeMillis, tick);
        TimeController.beginRenderTick(timeMillis, tick);
        return tickCount;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;tick(Z)V"))
    private void tick(RenderTickCounter.Dynamic instance, boolean paused) {
        instance.tick(paused);
        TimeController.standaloneRenderTickCounter.tick(paused);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;setTickFrozen(Z)V"))
    private void setTickFrozen(RenderTickCounter.Dynamic instance, boolean frozen) {
        instance.setTickFrozen(frozen);
        TimeController.standaloneRenderTickCounter.setTickFrozen(frozen);
    }
}
