package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.Matrix
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*


class OverclockState : PersistentState() {
    val overclockData = mutableMapOf<UUID, OverclockData>()

    companion object {
        private val type = Type({ OverclockState() }, OverclockState::createFromNbt, null)

        private fun createFromNbt(nbt: NbtCompound, lookup: RegistryWrapper.WrapperLookup): OverclockState {
            val state = OverclockState()

            val compound = nbt.getCompound("players")
            compound.keys.forEach {
                val manaOverclock = compound.getCompound(it).getDouble("ManaOverclock")
                val magicOverclock = compound.getCompound(it).getDouble("MagicOverclock")
                val overclockData = OverclockData(manaOverclock, magicOverclock)

                val uuid = UUID.fromString(it)
                state.overclockData[uuid] = overclockData
            }

            return state
        }

        @JvmStatic
        fun getPlayerState(player: LivingEntity): OverclockData {
            val overclockState = getServerState(player.world.server!!)
            val overclockData = overclockState.overclockData.computeIfAbsent(player.uuid) { OverclockData(1.0, 1.0) }

            return overclockData
        }

        private fun getServerState(server: MinecraftServer): OverclockState {
            val persistentStateManager = server.getWorld(World.OVERWORLD)!!.persistentStateManager

            val state = persistentStateManager.getOrCreate(type, Matrix.MOD_ID + "_overclock_state")

            state.markDirty()

            return state
        }
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
        val playersNbt = NbtCompound()
        overclockData.forEach { (uuid, overclockData) ->
            val playerNbt = NbtCompound()

            playerNbt.putDouble("ManaOverclock", overclockData.manaOverclock)
            playerNbt.putDouble("MagicOverclock", overclockData.magicOverclock)

            playersNbt.put(uuid.toString(), playerNbt)
        }
        nbt.put("players", playersNbt)

        return nbt
    }
}

val ServerPlayerEntity.magicClock
    get() = OverclockState.getPlayerState(this).magicOverclock

val ServerPlayerEntity.manaClock
    get() = OverclockState.getPlayerState(this).manaOverclock