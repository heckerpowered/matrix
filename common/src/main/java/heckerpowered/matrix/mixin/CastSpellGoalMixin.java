/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2: {@code SpellcasterIllager.CastSpellGoal} (Yarn) is now the abstract base
 * {@link SpellcasterIllager.SpellcasterUseSpellGoal}, with {@code canStart}/{@code shouldContinue}/
 * {@code castSpell} renamed to {@code canUse}/{@code canContinueToUse}/{@code performSpellCasting}
 * (the latter now {@code protected abstract}, implemented per-illager in subclasses such as
 * {@code EvokerAttackSpellGoal}/{@code IllusionerBlindnessSpellGoal}). The outer-instance
 * synthetic field is named {@code this$0} instead of the Yarn intermediary {@code field_7386}.
 */
@Mixin(SpellcasterIllager.SpellcasterUseSpellGoal.class)
class CastSpellGoalMixin {
    @Shadow
    @Final
    SpellcasterIllager this$0;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void canUse(CallbackInfoReturnable<Boolean> cir) {
        final var self = this$0;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    private void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        final var self = this$0;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void start(CallbackInfo ci) {
        final var self = this$0;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            ci.cancel();
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/illager/SpellcasterIllager$SpellcasterUseSpellGoal;performSpellCasting()V"))
    private void tick(SpellcasterIllager.SpellcasterUseSpellGoal instance) {
        final var self = this$0;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            return;
        }

        instance.performSpellCasting();
    }
}
