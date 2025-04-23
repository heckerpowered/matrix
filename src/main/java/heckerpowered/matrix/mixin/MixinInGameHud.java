package heckerpowered.matrix.mixin;

import heckerpowered.matrix.Matrix;
import heckerpowered.matrix.common.effect.MatrixStatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
class MixinInGameHud {
    private MixinInGameHud() {
    }

    @Inject(method = "renderMainHud", at = @At("HEAD"))
    private void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // UIBlurShader.startUIOverlayDrawing(context, tickCounter.getTickDelta(false));
    }

    @Redirect(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud$HeartType;getTexture(ZZZ)Lnet/minecraft/util/Identifier;"))
    private Identifier getTexture(InGameHud.HeartType instance, boolean hardcore, boolean half, boolean blinking) {
        final var minecraft = MinecraftClient.getInstance();
        final var player = minecraft.player;
        if (instance == InGameHud.HeartType.NORMAL && player != null && player.hasStatusEffect(MatrixStatusEffects.getBloodPactEffect())) {
            if (half) {
                return blinking ? Matrix.identifier("hud/heart/half_blinking") : Matrix.identifier("hud/heart/half");
            } else {
                return blinking ? Matrix.identifier("hud/heart/full_blinking") : Matrix.identifier("hud/heart/full");
            }
        }

        return instance.getTexture(hardcore, half, blinking);
    }
}
