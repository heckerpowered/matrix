/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor instead of class-tweaker field widenings; see {@link TickRateManagerAccessor}
 * for why the tweaker's accessible-field entries cannot be relied on in production.
 */
@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("waitingForNextTick")
    void matrix$setWaitingForNextTick(boolean value);

    @Accessor("nextTickTimeNanos")
    long matrix$getNextTickTimeNanos();

    @Accessor("nextTickTimeNanos")
    void matrix$setNextTickTimeNanos(long value);
}
