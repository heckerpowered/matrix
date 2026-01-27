/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpellcastingIllagerEntity.CastSpellGoal.class)
class CastSpellGoalMixin {
    @Shadow
    @Final
    SpellcastingIllagerEntity field_7386; // this

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void canStart(CallbackInfoReturnable<Boolean> cir) {
        final var self = field_7386;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    private void shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        final var self = field_7386;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void start(CallbackInfo ci) {
        final var self = field_7386;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            ci.cancel();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SpellcastingIllagerEntity$CastSpellGoal;castSpell()V"))
    private void tick(SpellcastingIllagerEntity.CastSpellGoal instance) {
        final var self = field_7386;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            return;
        }

        instance.castSpell();
    }
}
