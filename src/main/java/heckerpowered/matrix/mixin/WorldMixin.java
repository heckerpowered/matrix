/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static heckerpowered.matrix.common.item.LightningChestplate1.isBorrowedTime;

@Mixin(World.class)
class WorldMixin {
    private WorldMixin() {
    }

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void tickEntity(Consumer<Entity> tickConsumer, Entity entity, CallbackInfo ci) {
        if (entity instanceof final PlayerEntity player && isBorrowedTime(player)) {
            // Player is standalone ticking.
            ci.cancel();
        }
    }
}
