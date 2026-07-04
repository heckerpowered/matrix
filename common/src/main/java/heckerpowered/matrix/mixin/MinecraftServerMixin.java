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

/**
 * Time-warp server support, restored from 16a3466 ("Fix time warp"):
 * <p>
 * The waitForTasks HEAD hook keeps the integrated server responsive while a warp stretches
 * ticks to multiple seconds: 26.2's PacketProcessor queue is only drained at tick-top by
 * vanilla (and its enqueue does not wake the parked server thread), so serverbound packets —
 * including the warp-restore payload and player movement — are drained here on every wait
 * iteration instead, and waitingForNextTick is forced false so the wait uses the short-poll
 * branch rather than parking for the whole warped tick.
 * <p>
 * The interface is attached via the classtweaker's inject-interface entry plus the soft
 * {@code @Implements} below (NOT a direct {@code implements} on the mixin class).
 */
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
        // Util.getNanos(), not System.nanoTime(): this feeds nextTickTimeNanos math in
        // ServerTimeRatio, and the server loop's clock is Util's time source, which the
        // client JVM redirects to the GLFW timer (different epoch than System.nanoTime).
        matrix$tickStartTimeNanos = Util.getNanos();
    }

    public long matrix$getTickStartTimeNanos() {
        return matrix$tickStartTimeNanos;
    }

    public void matrix$setTickStartTimeNanos(long l) {
        matrix$tickStartTimeNanos = l;
    }
}
