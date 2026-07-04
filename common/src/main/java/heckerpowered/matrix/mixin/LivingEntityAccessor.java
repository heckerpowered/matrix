/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Mixin accessors instead of class-tweaker field widenings; see
 * {@link TickRateManagerAccessor} for why the tweaker's accessible entries cannot be relied
 * on in production.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("attackStrengthTicker")
    void matrix$setAttackStrengthTicker(int value);

    @Accessor("lastHurtByPlayer")
    void matrix$setLastHurtByPlayer(EntityReference<Player> value);

    @Accessor("lastHurtByPlayerMemoryTime")
    void matrix$setLastHurtByPlayerMemoryTime(int value);

    @Accessor("useItemRemaining")
    void matrix$setUseItemRemaining(int value);

    @Invoker("internalSetAbsorptionAmount")
    void matrix$internalSetAbsorptionAmount(float amount);
}
