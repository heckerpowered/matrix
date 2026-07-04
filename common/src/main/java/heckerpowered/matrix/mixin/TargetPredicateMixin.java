/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

/**
 * 26.2: {@code TargetPredicate#test(LivingEntity, LivingEntity)} (Yarn) gained a leading
 * {@code ServerLevel} parameter: {@code TargetingConditions#test(ServerLevel, LivingEntity, LivingEntity)}.
 */
@Mixin(TargetingConditions.class)
class TargetPredicateMixin {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void test(ServerLevel level, LivingEntity baseEntity, LivingEntity targetEntity, CallbackInfoReturnable<Boolean> cir) {
        if (targetEntity instanceof final Player player && isPhaseWalking(player)) {
            cir.setReturnValue(false);
        }
    }
}
