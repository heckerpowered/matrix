/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import heckerpowered.matrix.common.effect.ModMobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2: {@code EndermanEntity#damage(DamageSource, float)Z} (Yarn) is now split into the
 * server-authoritative {@code EnderMan#hurtServer(ServerLevel, DamageSource, float)Z}, which
 * is EnderMan's own override of {@code LivingEntity#hurtServer}. The mixin still extends
 * {@link LivingEntity} purely so {@code super.hurtServer(...)} resolves past EnderMan's (and
 * Monster's, which does not override it) dodge behaviour straight to the vanilla
 * {@link LivingEntity#hurtServer} implementation, exactly like the old {@code super.damage(...)}
 * call bypassed {@code EndermanEntity}'s override.
 * <p>
 * {@code EndermanEntity#teleportTo(DDD)Z} no longer exists: {@code Entity#teleportTo(DDD)} is
 * now {@code void}, and the boolean-returning teleport-execution logic Enderman used to
 * override lives in its own private {@code teleport(double, double, double)} method.
 * <p>
 * {@code LivingEntity#getStatusEffect} was renamed to {@code getEffect}.
 */
@Mixin(EnderMan.class)
abstract class EndermanEntityMixin extends LivingEntity {
    private EndermanEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @SuppressWarnings("all")
    @Unique
    private EnderMan self() {
        return (EnderMan) (Object) this;
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void hurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        final var self = (EnderMan) (Object) this;
        final var crippleMovement = ModMobEffects.INSTANCE.getCrippleMovement();
        final var effect = self().getEffect(crippleMovement);
        if (effect == null && !ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            return;
        }

        final var result = super.hurtServer(level, source, amount);
        cir.setReturnValue(result);
    }

    @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void teleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        final var self = (EnderMan) (Object) this;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            cir.setReturnValue(false);
        }
    }
}
