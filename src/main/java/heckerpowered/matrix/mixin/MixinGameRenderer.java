package heckerpowered.matrix.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
class MixinGameRenderer {
    private MixinGameRenderer() {
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo ci) {
        //UIBlurShader.setupDimensions(width, height);
    }

    @Inject(method = "render", at = @At(value = "RETURN"))
    private void hookRenderEventStop(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        // UIBlurShader.endUIOverlayDrawing();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.BEFORE))
    private void injectRenderBlur(CallbackInfo ci) {
        // if (!(MinecraftClient.getInstance().currentScreen instanceof ChatScreen)) {
        //     UIBlurShader.endUIOverlayDrawing();
        // }
    }
}
