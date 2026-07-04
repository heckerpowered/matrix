/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

/**
 * 26.2: the loyalty-level accessor field is {@code ID_LOYALTY} (was {@code LOYALTY}), read via
 * inherited {@code Entity#entityData} (was {@code dataTracker}). {@code isNoClip()} is now
 * {@code AbstractArrow#isNoPhysics()}, {@code asItemStack()} is {@code ThrownTrident#getWeaponItem()},
 * and {@code PlayerInventory#insertStack} is {@code Inventory#add}.
 */
@Mixin(ThrownTrident.class)
abstract class TridentEntityMixin extends AbstractArrow {
    @Shadow
    @Final
    private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Shadow
    private boolean dealtDamage;

    TridentEntityMixin(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        final var owner = getOwner();
        if (!(owner instanceof final Player player)) {
            return;
        }
        if (!isPhaseWalking(player)) {
            return;
        }
        int i = this.entityData.get(ID_LOYALTY);
        if (i > 0 && (dealtDamage || isNoPhysics())) {
            player.getInventory().add(((ThrownTrident) (Object) this).getWeaponItem());
            discard();
        }
    }
}
