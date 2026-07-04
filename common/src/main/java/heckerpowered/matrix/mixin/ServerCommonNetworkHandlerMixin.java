/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

/**
 * 26.2: {@code Entity#getWorld()} (Yarn) is now {@code Entity#level()}. The {@code send}
 * overload with a callback no longer takes {@code PacketSendListener} (now a static-helper-only
 * class) — it takes {@code io.netty.channel.ChannelFutureListener} directly.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
class ServerCommonNetworkHandlerMixin {
    @Unique
    @Nullable
    private Entity getEntity(Packet<?> packet) {
        if (!(this instanceof final ServerPlayerConnection handler)) {
            return null;
        }

        final var player = handler.getPlayer();
        final var world = player.level();
        if (packet instanceof final ClientboundMoveEntityPacket entityPacket) {
            return entityPacket.getEntity(world);
        }
        if (packet instanceof final ClientboundRotateHeadPacket entityPacket) {
            return entityPacket.getEntity(world);
        }
        if (packet instanceof final ClientboundSetEntityDataPacket entityPacket) {
            return world.getEntity(entityPacket.id());
        }

        return null;
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, @Nullable ChannelFutureListener callbacks, CallbackInfo ci) {
        final var targetEntity = getEntity(packet);
        if (targetEntity instanceof final Player player && isPhaseWalking(player)) {
            ci.cancel();
        }
    }
}
