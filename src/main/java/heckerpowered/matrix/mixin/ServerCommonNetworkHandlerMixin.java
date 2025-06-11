package heckerpowered.matrix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static heckerpowered.matrix.common.item.LightningChestplate1.isPhaseWalking;

@Mixin(ServerCommonNetworkHandler.class)
class ServerCommonNetworkHandlerMixin {
    @Unique
    @Nullable
    private Entity getEntity(Packet<?> packet) {
        if (!(this instanceof final PlayerAssociatedNetworkHandler handler)) {
            return null;
        }

        final var player = handler.getPlayer();
        final var world = player.getWorld();
        if (packet instanceof final EntityS2CPacket entityPacket) {
            return entityPacket.getEntity(world);
        }
        if (packet instanceof final EntitySetHeadYawS2CPacket entityPacket) {
            return entityPacket.getEntity(world);
        }
        if (packet instanceof final EntityTrackerUpdateS2CPacket entityPacket) {
            return world.getEntityById(entityPacket.id());
        }

        return null;
    }

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        final var targetEntity = getEntity(packet);
        if (targetEntity instanceof final PlayerEntity player && isPhaseWalking(player)) {
            ci.cancel();
        }
    }
}
