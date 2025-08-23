/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.entity.mob.GuardianEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuardianEntity.FireBeamGoal.class)
class FireBeamGoalMixin {
    @Shadow
    @Final
    private GuardianEntity guardian;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void canStart(CallbackInfoReturnable<Boolean> cir) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(guardian)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    private void shouldContinue(CallbackInfoReturnable<Boolean> cir) {
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
