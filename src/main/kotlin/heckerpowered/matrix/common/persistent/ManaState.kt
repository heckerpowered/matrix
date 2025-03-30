package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.network.SyncManaPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
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
                val isInfinite = compound.getCompound(it).getBoolean("IsInfinite")
                val manaData = ManaData(mana, maxMana, isInfinite)

                val uuid = UUID.fromString(it)
                state.manaData[uuid] = manaData
            }

            return state
        }

        @JvmStatic
        fun getPlayerState(player: LivingEntity): ManaData {
            val manaState = getServerState(player.world.server!!)
            val manaData = manaState.manaData.computeIfAbsent(player.uuid) { ManaData(100.0, 100.0) }

            return manaData
        }

        private fun getServerState(server: MinecraftServer): ManaState {
            val persistentStateManager = server.getWorld(World.OVERWORLD)!!.persistentStateManager

            val state = persistentStateManager.getOrCreate(type, Matrix.MOD_ID + "_mana_state")

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
            playerNbt.putBoolean("IsInfinite", manaData.isInfinite)

            playersNbt.put(uuid.toString(), playerNbt)
        }
        nbt.put("players", playersNbt)

        return nbt
    }
}

var ServerPlayerEntity.mana: Double
    get() = ManaState.getPlayerState(this).mana
    set(value) {
        val manaData = ManaState.getPlayerState(this)
        manaData.mana = value.coerceIn(.0, manaData.maxMana)

        ServerPlayNetworking.send(this, SyncManaPayload(manaData.mana, manaData.maxMana))
    }

var ServerPlayerEntity.maxMana: Double
    get() = ManaState.getPlayerState(this).maxMana
    set(value) {
        val manaData = ManaState.getPlayerState(this)
        manaData.maxMana = value.coerceAtLeast(.0)

        ServerPlayNetworking.send(this, SyncManaPayload(manaData.mana, manaData.maxMana))
    }

var ServerPlayerEntity.isInfiniteMana: Boolean
    get() = ManaState.getPlayerState(this).isInfinite
    set(value) {
        val manaData = ManaState.getPlayerState(this)
        manaData.isInfinite = value
    }

val PlayerEntity.isWizard: Boolean
    get() = getEquippedStack(EquipmentSlot.HEAD).item is WizardHelmet

val PlayerEntity.wizardHelmet: ItemStack
    get() = getEquippedStack(EquipmentSlot.HEAD)

val PlayerEntity.queueSize: Long
    get() {
        val wizardHelmet = this.wizardHelmet
        val item = wizardHelmet.item
        if (item !is WizardHelmet) {
            return 1
        }
        return item.getQueueSize(this, wizardHelmet)
    }