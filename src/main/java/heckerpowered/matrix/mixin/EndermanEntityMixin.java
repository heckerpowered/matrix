/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import heckerpowered.matrix.common.effect.MatrixStatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
abstract class EndermanEntityMixin extends LivingEntity {
    private EndermanEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @SuppressWarnings("all")
    @Unique
    private EndermanEntity self() {
        return (EndermanEntity) (Object) this;
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        final var self = (EndermanEntity) (Object) this;
        final var crippleMovement = MatrixStatusEffects.getCrippleMovementEffect();
        final var effect = self().getStatusEffect(crippleMovement);
        if (effect == null && !ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            return;
        }

        final var result = super.damage(source, amount);
        cir.setReturnValue(result);
    }

    @Inject(method = "teleportTo(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void teleportTo(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        final var self = (EndermanEntity) (Object) this;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            cir.setReturnValue(false);
        }
    }
}
