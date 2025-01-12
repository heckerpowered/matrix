package heckerpowered.matrix.common.network

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.TimeHelper
import net.minecraft.util.Util

data class WrapPayload(
    private val timeScale: Double
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<WrapPayload> = CustomPayload.id("wrap")
        val codec: PacketCodec<PacketByteBuf, WrapPayload> =
            PacketCodec.of(WrapPayload::encode) { buffer ->
                WrapPayload(
                    buffer.readDouble()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeDouble(timeScale)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return WrapPayload.id
    }

    fun handle(context: Context) {
        val server = context.server()

        val newTickTime = ((TimeHelper.SECOND_IN_NANOS / 20L) / timeScale).toLong()
        val previousTickTime = server.tickManager.nanosPerTick
        server.tickManager.nanosPerTick = newTickTime

        val currentTime = Util.getMeasuringTimeNano()
        val remainingTickTime = server.tickEndTimeNanos - currentTime
        val remainingRatio = remainingTickTime.toDouble() / previousTickTime.toDouble()

        val newTickEndTime = currentTime + (newTickTime * remainingRatio).toLong()

        server.tickStartTimeNanos = newTickEndTime
        server.tickEndTimeNanos = newTickEndTime + newTickTime
    }
}