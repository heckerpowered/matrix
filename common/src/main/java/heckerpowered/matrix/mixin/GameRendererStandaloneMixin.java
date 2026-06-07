/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.TimeController;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
class GameRendererStandaloneMixin {
    private GameRendererStandaloneMixin() {
    }

    @ModifyVariable(method = "renderItemInHand", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float useStandaloneHandTickDelta(float tickDelta) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            return TimeController.standaloneRenderTickCounter.getTickDelta(true);
        }
        return tickDelta;
    }
}
