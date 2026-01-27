/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.item.WardenChestplateItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

@Mixin(ServerPlayNetworkHandler.class)
class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    private ServerPlayNetworkHandlerMixin() {
    }

    @Inject(method = "onPlayerInteractItem", at = @At("TAIL"))
    private void onPlayerInteractItem(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        final var instantUse = WardenChestplateItem.isAngered(player) || isPhaseWalking(player);
        if (instantUse && player.isUsingItem()) {
            final var activeItem = player.getActiveItem();
            player.itemUseTimeLeft = 0;
            player.stopUsingItem();
            if (!activeItem.isUsedOnRelease()) {
                activeItem.finishUsing(player.getWorld(), player);
            }
        }
    }
}
