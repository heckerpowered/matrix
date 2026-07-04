/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.world.entity.ai.behavior.warden.SonicBoom;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2: the old {@code Task<Warden>} API ({@code shouldRun}/{@code shouldKeepRunning}/
 * {@code run}/{@code keepRunning}) was replaced by {@code Behavior<Warden>}'s
 * {@code checkExtraStartConditions}/{@code canStillUse}/{@code start}/{@code tick}. Each has a
 * generic {@code LivingEntity}-typed bridge overload, so the {@code Warden}-typed descriptor is
 * specified explicitly to target the real (non-bridge) method.
 */
@Mixin(SonicBoom.class)
class SonicBoomTaskMixin {
    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;)Z", at = @At("HEAD"), cancellable = true)
    private void checkExtraStartConditions(ServerLevel serverWorld, Warden wardenEntity, CallbackInfoReturnable<Boolean> cir) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canStillUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)Z", at = @At("HEAD"), cancellable = true)
    private void canStillUse(ServerLevel serverWorld, Warden wardenEntity, long l, CallbackInfoReturnable<Boolean> cir) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)V", at = @At("HEAD"), cancellable = true)
    private void start(ServerLevel serverWorld, Warden wardenEntity, long l, CallbackInfo ci) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/monster/warden/Warden;J)V", at = @At("HEAD"), cancellable = true)
    private void tick(ServerLevel serverWorld, Warden wardenEntity, long l, CallbackInfo ci) {
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(wardenEntity)) {
            ci.cancel();
        }
    }
}
