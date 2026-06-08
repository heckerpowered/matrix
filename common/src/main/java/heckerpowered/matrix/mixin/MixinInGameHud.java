/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import heckerpowered.matrix.client.MatrixHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Hud.class)
class MixinInGameHud {
    private MixinInGameHud() {
    }

    @Redirect(
            method = "extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
            )
    )
    private void translateCrosshair(GuiGraphicsExtractor extractor, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height) {
        if (width != 15 || height != 15) {
            extractor.blitSprite(pipeline, sprite, x, y, width, height);
            return;
        }

        Vector2f translatedCrosshairPosition = MatrixHud.translateCrosshairPosition(x, y);
        extractor.blitSprite(pipeline, sprite, (int) translatedCrosshairPosition.x, (int) translatedCrosshairPosition.y, width, height);
    }
}
