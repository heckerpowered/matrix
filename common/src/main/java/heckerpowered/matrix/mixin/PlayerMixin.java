/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import heckerpowered.matrix.Matrix;
import heckerpowered.matrix.common.combat.damage.*;
import heckerpowered.matrix.common.entity.rule.EntityRulePipeline;
import heckerpowered.matrix.common.entity.rule.LivingDeathContext;
import heckerpowered.matrix.common.item.WardenChestplateItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

/**
 * 26.2: {@code Player#applyDamage} no longer exists. The armor/magic-absorption and
 * exhaustion logic it used to hold is now inlined directly into {@code Player#actuallyHurt}
 * (a full override that does not delegate to {@link net.minecraft.world.entity.LivingEntity#actuallyHurt},
 * so {@code LivingEntityMixin} does not observe player damage). The realization/outcome/
 * settlement pipeline that used to run in {@code applyDamage} is re-anchored there.
 * <p>
 * {@code Player#attack} was also decomposed: the sprint/critical/sweep booleans are still
 * computed inline, but the sprint-reset and knockback-velocity writes moved into
 * {@code causeExtraKnockback}, and the damage-indicator particle spawn moved into
 * {@code damageStatsAndHearts}. The critical/sweep-attack flags are now sourced from the
 * private helper methods {@code canCriticalAttack}/{@code isSweepAttack}, which are
 * overridden directly via {@link ModifyReturnValue} instead of brittle local-variable ordinals.
 */
@Mixin(Player.class)
class PlayerMixin {
    private PlayerMixin() {
    }

    @Unique
    private Player self() {
        return (Player) (Object) this;
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void die(DamageSource source, CallbackInfo ci, @Local(argsOnly = true, name = "source") LocalRef<DamageSource> sourceReference) {
        final var context = new LivingDeathContext(self(), source);
        EntityRulePipeline.onLivingDeath(context);
        if (!context.getAllow()) {
            context.applyDecision();
            ci.cancel();
            return;
        }
        if (context.getDamageSource() != source) {
            sourceReference.set(context.getDamageSource());
        }
    }

    @ModifyVariable(
            method = "actuallyHurt",
            at = @At("HEAD"),
            argsOnly = true
    )
    private DamageSource unwrapSource(DamageSource source, @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference) {
        if (source instanceof final DamageSourceEnvelope envelope) {
            rawDamageReference.set(envelope.getRawDamage());
            return envelope.getOrigin();
        }
        return source;
    }

    @ModifyArg(method = "damageStatsAndHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"), index = 4)
    private int damageStatsAndHearts(int count) {
        return Math.min(count, 512);
    }


    @SuppressWarnings("DuplicatedCode")
    @Definition(id = "getDamageAfterMagicAbsorb", method = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F")
    @Expression("? = ?.getDamageAfterMagicAbsorb(?, ?)")
    @ModifyVariable(
            method = "actuallyHurt",
            at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), argsOnly = true)
    private float actuallyHurt(
            float amount,
            @Local(argsOnly = true) DamageSource source,
            @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference) {
        final var self = self();
        final var rawDamage = rawDamageReference.get();
        final var realizationContext = new DamageRealizationContext(self, source, rawDamage, amount);
        DamagePipeline.realization(realizationContext);

        final var retention = realizationContext.getRetention();
        final var realizedDamage = realizationContext.getRealizedDamage();
        final var outcomeContext = new DamageOutcomeContext(self, source, rawDamage, amount, retention);
        DamagePipeline.outcome(outcomeContext);

        final var settlementContext = new DamageSettlementContext(self, source, rawDamage, amount, realizedDamage);
        DamagePipeline.settlement(settlementContext);

        return settlementContext.getRemainingDamage();
    }

    @Inject(method = "getAttackStrengthScale", at = @At(value = "HEAD"), cancellable = true)
    private void getAttackStrengthScale(float baseTime, CallbackInfoReturnable<Float> cir) {
        if (WardenChestplateItem.isAngered(self()) || isPhaseWalking(self())) {
            cir.setReturnValue(1.0F);
        }
    }

    @ModifyReturnValue(method = "canCriticalAttack", at = @At("RETURN"))
    private boolean canCriticalAttack(boolean isCritical) {
        if (WardenChestplateItem.isAngered(self()) || isPhaseWalking(self())) {
            return true;
        }
        return isCritical;
    }

    @ModifyReturnValue(method = "isSweepAttack", at = @At("RETURN"))
    private boolean isSweepAttack(boolean canSweep) {
        if (WardenChestplateItem.isAngered(self()) || isPhaseWalking(self())) {
            return true;
        }
        return canSweep;
    }

    @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"))
    private void setSprinting(Player entity, boolean sprinting) {
        if (entity == self() && WardenChestplateItem.isAngered(self())) {
            return;
        }

        entity.setSprinting(sprinting);
    }

    @Redirect(method = "causeExtraKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void setDeltaMovement(Player entity, Vec3 velocity) {
        if (entity == self() && WardenChestplateItem.isAngered(self())) {
            return;
        }

        entity.setDeltaMovement(velocity);
    }
}
