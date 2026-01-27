/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import heckerpowered.matrix.common.event.LivingDamageCallback;
import heckerpowered.matrix.common.event.LivingDamageEvent;
import heckerpowered.matrix.common.item.WardenChestplateItem;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

@Mixin(PlayerEntity.class)
class PlayerEntityMixin {
    private PlayerEntityMixin() {
    }

    @Unique
    private PlayerEntity self() {
        return (PlayerEntity) (Object) this;
    }

    @Definition(id = "modifyAppliedDamage", method = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F")
    @Expression("? = ?.modifyAppliedDamage(?, ?)")
    @Inject(method = "applyDamage", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), cancellable = true)
    private void applyDamage(DamageSource source, float amount, CallbackInfo ci, @Local(argsOnly = true) LocalRef<DamageSource> sourceReference, @Local(argsOnly = true) LocalFloatRef amountReference) {
        final var livingDamageEvent = new LivingDamageEvent(self(), source, amount);
        final var result = LivingDamageCallback.EVENT.invoker().onHurt(livingDamageEvent);
        if (result == ActionResult.FAIL) {
            ci.cancel();
        }

        sourceReference.set(livingDamageEvent.getDamageSource());
        amountReference.set(livingDamageEvent.getAmount());
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
