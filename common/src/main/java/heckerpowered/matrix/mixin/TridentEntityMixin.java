/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

@Mixin(TridentEntity.class)
abstract class TridentEntityMixin extends PersistentProjectileEntity {
    @Shadow
    @Final
    private static TrackedData<Byte> LOYALTY;

    @Shadow
    private boolean dealtDamage;

    TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        final var owner = getOwner();
        if (!(owner instanceof final PlayerEntity player)) {
            return;
        }
        if (!isPhaseWalking(player)) {
            return;
        }
        int i = this.dataTracker.get(LOYALTY);
        if (i > 0 && (dealtDamage || isNoClip())) {
            player.getInventory().insertStack(asItemStack());
            discard();
        }
    }
}
