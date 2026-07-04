/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

/**
 * 26.2: {@code TridentItem#onStoppedUsing} (Yarn) is now {@code releaseUsing}, and the plain
 * (non-Riptide) throw no longer builds a {@code ThrownTrident} first and then calls
 * {@code setVelocity(Entity, F,F,F,F,F)} on it — it constructs-and-shoots atomically via the
 * static factory {@link net.minecraft.world.entity.projectile.Projectile#spawnProjectileFromRotation}
 * (args: factory, level, stack, shooter, pitch=0, velocity=2.5, divergence=1). The old
 * {@code speed} doubling on phase-walking shooters is re-anchored to the {@code velocity} arg
 * of that call, keyed off the {@code shooter} arg instead of a redirect receiver.
 */
@Mixin(TridentItem.class)
abstract class TridentItemMixin {
    TridentItemMixin() {
    }

    @ModifyArgs(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;"
            )
    )
    private void spawnProjectileFromRotation(Args args) {
        final LivingEntity shooter = args.get(3);
        if (shooter instanceof final Player player && isPhaseWalking(player)) {
            final float velocity = args.get(5);
            args.set(5, velocity * 2.0F);
        }
    }
}
