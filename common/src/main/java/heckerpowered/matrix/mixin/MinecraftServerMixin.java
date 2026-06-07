/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.core.ServerTickTiming;
import heckerpowered.matrix.core.ServerTimeRatio;
import heckerpowered.matrix.core.ServerTimeWarpLease;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
class MinecraftServerMixin {

    @Shadow
    @Final
    private ServerTickRateManager tickRateManager;

    private MinecraftServerMixin() {
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void tick(BooleanSupplier haveTime, CallbackInfo ci) {
        final var tickStartTimeNanos = System.nanoTime();
        ServerTickTiming.update(
                (MinecraftServer) (Object) this,
                tickStartTimeNanos,
                tickStartTimeNanos + tickRateManager.nanosecondsPerTick()
        );
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void stopServer(CallbackInfo ci) {
        matrix$restoreNormalTime();
    }

    @Inject(method = "saveEverything", at = @At("HEAD"))
    private void saveEverything(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> ci) {
        matrix$restoreNormalTime();
    }

    @Inject(method = "saveAllChunks", at = @At("HEAD"))
    private void saveAllChunks(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> ci) {
        matrix$restoreNormalTime();
    }

    private void matrix$restoreNormalTime() {
        final var server = (MinecraftServer) (Object) this;
        ServerTimeWarpLease.clear(server);
        ServerTimeRatio.restoreNormalTickDuration(server);
    }
}
