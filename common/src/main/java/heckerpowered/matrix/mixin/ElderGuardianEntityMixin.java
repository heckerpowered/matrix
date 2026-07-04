/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.effect.ManaOverloadEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;

/**
 * 26.2: {@code ElderGuardianEntity#mobTick} (Yarn) is now {@code customServerAiStep}, and
 * {@code MobEffectUtil#addEffectToPlayersWithinDistance} was renamed to
 * {@code addEffectToPlayersAround} (same signature/semantics).
 */
@Mixin(ElderGuardian.class)
class ElderGuardianEntityMixin {
    @Redirect(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectUtil;addEffectToPlayersAround(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;DLnet/minecraft/world/effect/MobEffectInstance;I)Ljava/util/List;"))
    private List<ServerPlayer> addEffectToPlayersAround(ServerLevel world, @Nullable Entity entity, Vec3 origin, double range, MobEffectInstance statusEffectInstance, int duration) {
        final var self = (ElderGuardian) (Object) this;
        if (ManaOverloadEffect.INSTANCE.isMagicAbilityDisabled(self)) {
            return Collections.emptyList();
        }

        return MobEffectUtil.addEffectToPlayersAround(world, entity, origin, range, statusEffectInstance, duration);
    }
}
