/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.extension.MatrixMinecraftServer;
import net.minecraft.network.PacketProcessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
@Implements(@Interface(iface = MatrixMinecraftServer.class, prefix = "matrix$"))
abstract class MinecraftServerMixin extends ReentrantBlockableEventLoop<TickTask> {
    @Shadow
    public boolean waitingForNextTick;
    @Unique
    long matrix$tickStartTimeNanos;

    @Shadow
    @Final
    private PacketProcessor packetProcessor;

    public MinecraftServerMixin(String name, boolean propagatesCrashes) {
        super(name, propagatesCrashes);
    }

    @Inject(method = "waitForTasks", at = @At("HEAD"))
    private void waitForTasks(CallbackInfo ci) {
        waitingForNextTick = false;
        packetProcessor.processQueuedPackets();
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void tick(BooleanSupplier haveTime, CallbackInfo ci) {
        matrix$tickStartTimeNanos = Util.getNanos();
    }

    public long matrix$getTickStartTimeNanos() {
        return matrix$tickStartTimeNanos;
    }

    public void matrix$setTickStartTimeNanos(long l) {
        matrix$tickStartTimeNanos = l;
    }
}
