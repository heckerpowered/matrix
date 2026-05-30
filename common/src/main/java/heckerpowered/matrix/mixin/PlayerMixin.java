/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
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
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

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
            method = "applyDamage",
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

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"))
    private int attack(int count) {
        return Math.min(count, 512);
    }


    @SuppressWarnings("DuplicatedCode")
    @Definition(id = "modifyAppliedDamage", method = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F")
    @Expression("? = ?.modifyAppliedDamage(?, ?)")
    @ModifyVariable(
            method = "applyDamage",
            at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), argsOnly = true)
    private float applyDamage(
            float amount,
            @Local(argsOnly = true) DamageSource source,
            @Share(value = "rawDamage", namespace = Matrix.MOD_ID) LocalFloatRef rawDamageReference) {
        final var self = (PlayerEntity) (Object) this;
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

    @Inject(method = "getAttackCooldownProgress", at = @At(value = "HEAD"), cancellable = true)
    private void getAttackCooldownProgress(float baseTime, CallbackInfoReturnable<Float> cir) {
        if (WardenChestplateItem.isAngered(self()) || isPhaseWalking(self())) {
            cir.setReturnValue(1.0F);
        }
    }

    @ModifyVariable(method = "attack", at = @At(value = "LOAD"), ordinal = 2)
    private boolean modifyAttackCritical(boolean isCritical) {
        if (WardenChestplateItem.isAngered(self()) || isPhaseWalking(self())) {
            return true;
        }
        return isCritical;
    }

    @ModifyVariable(method = "attack", at = @At(value = "LOAD"), ordinal = 3)
    private boolean modifyAttackSweep(boolean canSweep) {
        if (WardenChestplateItem.isAngered(self()) || isPhaseWalking(self())) {
            return true;
        }
        return canSweep;
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"))
    private void setSprinting(PlayerEntity player, boolean sprinting) {
        if (WardenChestplateItem.isAngered(player)) {
            return;
        }

        player.setSprinting(sprinting);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void setVelocity(PlayerEntity player, Vec3d velocity) {
        if (player == self() && WardenChestplateItem.isAngered(player)) {
            return;
        }

        player.setVelocity(velocity);
    }
}
