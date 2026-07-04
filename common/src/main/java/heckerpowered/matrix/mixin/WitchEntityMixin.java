/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2: {@code WitchEntity#shootAt} (Yarn) is now {@code Witch#performRangedAttack}
 * (the {@code RangedAttackMob} interface method).
 */
@Mixin(Witch.class)
class WitchEntityMixin {
    @Inject(method = "performRangedAttack", at = @At("HEAD"), cancellable = true)
    private void performRangedAttack(LivingEntity target, float pullProgress, CallbackInfo ci) {
        final var self = (Witch) (Object) this;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            ci.cancel();
        }
    }
}
