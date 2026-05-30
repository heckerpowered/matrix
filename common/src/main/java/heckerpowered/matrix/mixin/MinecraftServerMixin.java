/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.extension.MatrixMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
@Implements(@Interface(iface = MatrixMinecraftServer.class, prefix = "matrix$"))
class MinecraftServerMixin implements MatrixMinecraftServer {

    @Shadow
    @Final
    private ServerTickRateManager tickRateManager;

    @Unique
    long matrix$tickStartTimeNanos;

    @Unique
    long matrix$tickEndTimeNanos;



    private MinecraftServerMixin() {
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void tick(BooleanSupplier haveTime, CallbackInfo ci) {
        matrix$tickStartTimeNanos = System.nanoTime();
        matrix$tickEndTimeNanos = matrix$tickStartTimeNanos + tickRateManager.nanosecondsPerTick();
    }

    public long matrix$getTickStartTimeNanos() {
        return matrix$tickStartTimeNanos;
    }

    public void matrix$setTickStartTimeNanos(long l) {
        matrix$tickStartTimeNanos = l;
    }

    public long matrix$getTickEndTimeNanos() {
        return matrix$tickEndTimeNanos;
    }

    public void matrix$setTickEndTimeNanos(long l) {
        matrix$tickEndTimeNanos = l;
    }
}
