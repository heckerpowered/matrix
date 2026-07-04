/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.world.TickRateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin accessor instead of a class-tweaker field widening: in production the tweaker's
 * interface injections apply but the accessible-field entries did NOT reach this class
 * (IllegalAccessError on first slow-time engage), while mixin accessors behave identically
 * in dev and production.
 */
@Mixin(TickRateManager.class)
public interface TickRateManagerAccessor {
    @Accessor("nanosecondsPerTick")
    long matrix$getNanosecondsPerTick();

    @Accessor("nanosecondsPerTick")
    void matrix$setNanosecondsPerTick(long value);
}
