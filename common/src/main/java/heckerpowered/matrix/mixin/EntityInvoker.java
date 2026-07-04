/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** See {@link TickRateManagerAccessor} for why this is an invoker, not a tweaker widening. */
@Mixin(Entity.class)
public interface EntityInvoker {
    @Invoker("isInvulnerableToBase")
    boolean matrix$isInvulnerableToBase(DamageSource damageSource);
}
