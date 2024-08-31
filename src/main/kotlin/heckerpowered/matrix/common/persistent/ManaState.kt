package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.Matrix
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import java.util.*


class ManaState : PersistentState() {
    val manaData = mutableMapOf<UUID, ManaData>()

    companion object {
        private val type = Type({ ManaState() }, ManaState::createFromNbt, null)

        private fun createFromNbt(nbt: NbtCompound, lookup: RegistryWrapper.WrapperLookup): ManaState {
            val state = ManaState()

            val compound = nbt.getCompound("players")
            compound.keys.forEach {
                val mana = compound.getCompound(it).getDouble("Mana")
                val maxMana = compound.getCompound(it).getDouble("MaxMana")
                val manaData = ManaData(mana, maxMana)

                val uuid = UUID.fromString(it)
                state.manaData[uuid] = manaData
            }

            return state
        }

        @JvmStatic
        fun getPlayerState(player: LivingEntity): ManaData {
            val manaState = getServerState(player.world.server!!)
            val manaData = manaState.manaData.computeIfAbsent(player.uuid) { ManaData(1000.0, 1000.0) }

            return manaData
        }

        private fun getServerState(server: MinecraftServer): ManaState {
            val persistentStateManager = server.getWorld(World.OVERWORLD)!!.persistentStateManager

            val state = persistentStateManager.getOrCreate(type, Matrix.MOD_ID)

            state.markDirty()

            return state
        }
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
        val playersNbt = NbtCompound()
        manaData.forEach { (uuid, manaData) ->
            val playerNbt = NbtCompound()

            playerNbt.putDouble("Mana", manaData.mana)
            playerNbt.putDouble("MaxMana", manaData.maxMana)

            playersNbt.put(uuid.toString(), playerNbt)
        }
        nbt.put("players", playersNbt)

        return nbt
    }
}