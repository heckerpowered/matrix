package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.persistent.ManaState
import heckerpowered.matrix.common.persistent.OverclockState
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class OverclockPayload(
    private val manaOverclock: Double,
    private val magicOverclock: Double
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<OverclockPayload> = CustomPayload.id("overclock")
        val codec: PacketCodec<PacketByteBuf, OverclockPayload> =
            PacketCodec.of(OverclockPayload::encode) { buffer ->
                OverclockPayload(
                    buffer.readDouble(),
                    buffer.readDouble()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeDouble(manaOverclock)
        buffer.writeDouble(magicOverclock)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return OverclockPayload.id
    }

    fun handle(context: Context) {
        val player = context.player()
        val overclockData = OverclockState.getPlayerState(player)

        val previousManaOverclock = overclockData.manaOverclock

        overclockData.manaOverclock = manaOverclock.coerceIn(1.0, 10.0)
        overclockData.magicOverclock = magicOverclock.coerceIn(1.0, 10.0)

        // If the mana is overclocking, the difference is positive,
        // if the mana is underclocking, the difference is negative.
        val difference = manaOverclock - previousManaOverclock
        val maxManaDifference = difference * 100

        val manaState = ManaState.getPlayerState(player)
        manaState.maxMana += maxManaDifference
        if (manaState.mana > manaState.maxMana) {
            manaState.mana = manaState.maxMana
        }
        ServerPlayNetworking.send(player, SyncManaPayload(manaState.mana, manaState.maxMana))
    }
}