/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.client.TimeController;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DeltaTracker.Timer.class)
class DeltaTrackerTimerMixin {
    @Shadow
    private float deltaTicks;

    @Shadow
    private float deltaTickResidual;

    @Shadow
    private long lastMs;

    @Shadow
    @Final
    private float msPerTick;

    @Shadow
    @Final
    private FloatUnaryOperator targetMsptProvider;

    private DeltaTrackerTimerMixin() {
    }

    @Inject(method = "advanceGameTime", at = @At("HEAD"), cancellable = true)
    private void advanceGameTime(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        if (!TimeController.shouldScaleClientGameTime()) {
            return;
        }

        final var timeScale = TimeController.getClientGameTimeScale();
        final var targetMsPerTick = targetMsptProvider.apply(msPerTick);
        if (targetMsPerTick <= 0.0F) {
            return;
        }

        deltaTicks = (timeMillis - lastMs) / (targetMsPerTick / timeScale);
        lastMs = timeMillis;
        deltaTickResidual += deltaTicks;
        final var ticks = (int) deltaTickResidual;
        deltaTickResidual -= ticks;
        cir.setReturnValue(ticks);
    }
}
