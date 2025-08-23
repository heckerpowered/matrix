/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

@Mixin(TridentItem.class)
abstract class TridentItemMixin {
    TridentItemMixin() {
    }

    @Redirect(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/TridentEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V"))
    private void setVelocity(TridentEntity trident, Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        if (shooter instanceof final PlayerEntity player && isPhaseWalking(player)) {
            speed *= 2.0F;
        }

        trident.setVelocity(shooter, pitch, yaw, roll, speed, divergence);
    }
}
