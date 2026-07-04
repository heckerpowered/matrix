/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2: {@code GuardianEntity.FireBeamGoal} (Yarn) no longer exists as a separate goal —
 * vanilla merged the laser-beam attack into {@link Guardian.GuardianAttackGoal}, which now
 * owns targeting/movement AND the beam charge-up/damage that {@code FireBeamGoal} used to
 * handle alone. The {@code guardian} field and {@code canStart}/{@code shouldContinue}/
 * {@code start}/{@code tick} lifecycle are preserved 1:1 under their new
 * {@code canUse}/{@code canContinueToUse}/{@code start}/{@code tick} names.
 */
@Mixin(Guardian.GuardianAttackGoal.class)
class FireBeamGoalMixin {
    @Shadow
    @Final
    private Guardian guardian;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(guardian)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(guardian)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void start(CallbackInfo ci) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(guardian)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(guardian)) {
            ci.cancel();
        }
    }
}
