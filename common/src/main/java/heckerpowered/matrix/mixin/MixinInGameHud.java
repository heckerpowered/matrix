/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import heckerpowered.matrix.Matrix;
import heckerpowered.matrix.client.MatrixHud;
import heckerpowered.matrix.common.effect.ModMobEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2: InGameHud became the extract-based {@link Hud}; renderHeart/renderCrosshair are now
 * extractHeart/extractCrosshair, HeartType.getTexture is getSprite, and the crosshair is
 * positioned directly through blitSprite's x/y (no pose translate), so the former
 * Matrix4fStack.translate redirect is folded into the blitSprite redirect.
 */
@Mixin(Hud.class)
class MixinInGameHud {
    private MixinInGameHud() {
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        // UIBlurShader.startUIOverlayDrawing(context, tickCounter.getTickDelta(false));
    }

    @Redirect(method = "extractHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud$HeartType;getSprite(ZZZ)Lnet/minecraft/resources/Identifier;"))
    private Identifier getSprite(Hud.HeartType instance, boolean hardcore, boolean half, boolean blinking) {
        final var minecraft = Minecraft.getInstance();
        final var player = minecraft.player;
        if (instance == Hud.HeartType.NORMAL && player != null && player.hasEffect(ModMobEffects.INSTANCE.getBloodPact())) {
            if (half) {
                return blinking ? Matrix.identifier("hud/heart/half_blinking") : Matrix.identifier("hud/heart/half");
            } else {
                return blinking ? Matrix.identifier("hud/heart/full_blinking") : Matrix.identifier("hud/heart/full");
            }
        }

        return instance.getSprite(hardcore, half, blinking);
    }

    @Redirect(method = "extractCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void translateCrosshair(GuiGraphicsExtractor instance, RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height) {
        if (width != 15 || height != 15) {
            instance.blitSprite(pipeline, texture, x, y, width, height);
            return;
        }
        final var translatedCrosshairPosition = MatrixHud.translateCrosshairPosition(x, y);
        instance.blitSprite(pipeline, texture, (int) translatedCrosshairPosition.x, (int) translatedCrosshairPosition.y, width, height);
    }
}
