package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.shader.UIBlurShader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
class MixinInGameHud {
    private MixinInGameHud() {
    }

    @Inject(method = "renderMainHud", at = @At("HEAD"))
    private void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        UIBlurShader.startUIOverlayDrawing(context, tickCounter.getTickDelta(false));
    }
}
