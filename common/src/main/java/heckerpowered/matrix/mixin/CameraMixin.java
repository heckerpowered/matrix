/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.TimeController;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
class CameraMixin {
    private CameraMixin() {
    }

    @Inject(method = "getCameraEntityPartialTicks", at = @At("RETURN"), cancellable = true)
    private void getCameraEntityPartialTicks(DeltaTracker deltaTracker, CallbackInfoReturnable<Float> cir) {
        if (TimeController.getPlayerStandaloneRenderTick()) {
            cir.setReturnValue(TimeController.standaloneRenderTickCounter.getTickDelta(true));
        }
    }
}
