/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.item.WardenChestplateItem;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

@Mixin(ServerGamePacketListenerImpl.class)
class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayer player;

    private ServerPlayNetworkHandlerMixin() {
    }

    @Inject(method = "handleUseItem", at = @At("TAIL"))
    private void onPlayerInteractItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        final var instantUse = WardenChestplateItem.isAngered(player) || isPhaseWalking(player);
        if (instantUse && player.isUsingItem()) {
            final var activeItem = player.getActiveItem();
            // Cross-class write of a protected LivingEntity field: must go through the
            // accessor — the class-tweaker widening does not apply in production.
            ((LivingEntityAccessor) player).matrix$setUseItemRemaining(0);
            player.stopUsingItem();
            if (!activeItem.useOnRelease()) {
                activeItem.finishUsingItem(player.level(), player);
            }
        }
    }
}
