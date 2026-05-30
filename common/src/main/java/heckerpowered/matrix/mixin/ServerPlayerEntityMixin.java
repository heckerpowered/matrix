/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import heckerpowered.matrix.common.entity.rule.EntityRulePipeline;
import heckerpowered.matrix.common.entity.rule.LivingDeathContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
class ServerPlayerEntityMixin {
    private ServerPlayerEntityMixin() {
    }

    @Unique
    private ServerPlayer self() {
        return (ServerPlayer) (Object) this;
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
}
