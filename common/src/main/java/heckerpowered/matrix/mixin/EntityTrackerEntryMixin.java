/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.common.magic.system.MagicSystem;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * 26.2: {@code EntityTrackerEntry#sendPackets} (Yarn) — invoked once when a player starts
 * tracking (pairs with) an entity — is now {@code ServerEntity#sendPairingData}.
 */
@Mixin(ServerEntity.class)
class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "sendPairingData", at = @At("TAIL"))
    private void sendPairingData(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> sender, CallbackInfo ci) {
        MagicSystem.onEntityTracked(player, entity);
    }
}
