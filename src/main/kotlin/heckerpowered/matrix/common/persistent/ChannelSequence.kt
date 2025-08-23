/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.ChannelAnimation
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.event.ReadDataCallback
import heckerpowered.matrix.common.event.WriteDataCallback
import heckerpowered.matrix.common.magics.ChannelingMagic
import heckerpowered.matrix.common.magics.MagicData
import heckerpowered.matrix.common.network.ChannelMagicPayload
import heckerpowered.matrix.common.network.SyncHealthPayload
import heckerpowered.matrix.core.MatrixLivingEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

/**
 * Magics that channeled at the same time are called channeling sequence. The channeling sequence will not be cleared
 * until all magics in the sequence are casted.
 *
 * @see Magic
 */
class ChannelSequence(
    /**
     * The player who casted the channeling sequence.
     */
    var player: PlayerEntity?,

    /**
     * The UUID of the player who casted the channeling sequence.
     */
    private var playerUUID: UUID,

    /**
     * The target of the channeling sequence.
     */
    val target: LivingEntity,

    /**
     * The magics in the channeling sequence.
     */
    val magics: MutableList<ChannelingMagic>,
) {
    /**
     * Whether the channeling sequence is locked.
     */
    var locked = false

    companion object {
        fun channelMagic(
            magic: Magic,
            player: PlayerEntity,
            target: LivingEntity,
            costMana: Boolean = true,
            bypassLock: Boolean = false,
            data: MagicData = MagicData(),
        ): Boolean {
            if (target !is MatrixLivingEntity) return false

            val sequences = target.getChannelSequence()
            val channelSequence = sequences.computeIfAbsent(player.uuid) {
                ChannelSequence(player, player.uuid, target, mutableListOf())
            }.apply {
                this.player = player
                this.playerUUID = player.uuid
            }

            if (channelSequence.locked && !bypassLock) {
                return false
            }

            val channelTime = magic.getChannelTime(player, target, channelSequence, data)
            val cost = magic.getCost(player, target, channelSequence, data)
            val convertRatio = magic.getBloodPactConvertRatio(player, target, channelSequence, data)

            fun performChannel() {
                channelSequence.magics.add(ChannelingMagic(magic, 0, channelTime, cost, data))
                magic.channel(player, target, channelSequence, data)
                if (player is ServerPlayerEntity) {
                    ServerPlayNetworking.send(player, ChannelMagicPayload(magic.id, target.id, channelTime))
                }
            }

            if (player is ServerPlayerEntity) {
                when {
                    player.isInfiniteMana || !costMana -> {
                        performChannel()
                    }

                    player.mana >= cost -> {
                        player.mana -= cost
                        performChannel()
                    }

                    player.bloodPactActive && (player.mana + player.health * convertRatio >= cost) -> {
                        val usedHealth = (cost - player.mana) / convertRatio
                        player.health = maxOf(player.health - usedHealth.toFloat(), 1f)
                        player.mana = 0.0
                        ServerPlayNetworking.send(player, SyncHealthPayload(player))
                        performChannel()
                    }

                    else -> return false
                }
            } else {
                performChannel()
            }

            return true
        }

        fun getChannelSequence(player: PlayerEntity, target: LivingEntity?): ChannelSequence? {
            if (target !is MatrixLivingEntity) {
                return null
            }

            return target.getChannelSequence()[player.uuid]
        }

        @Environment(EnvType.CLIENT)
        fun channelMagicClient(magic: ChannelingMagic, target: LivingEntity, channelTime: Long = magic.channelTime) {
            ChannelSequenceRenderer
                .channelSequenceAnimationMap
                .computeIfAbsent(target) { mutableListOf() }
                .add(ChannelAnimation(magic.magic).also {
                    it.channelTime = channelTime
                    it.initialProgressOffset = minecraft.getRenderTickCounter().getTickDelta(true)
                })
            ChannelSequenceRenderer.offsetAnimationMap
                .computeIfAbsent(target) { ChannelSequenceRenderer.Companion.OffsetAnimation() }
        }

        fun onInitialize() {
            EntityTickCallback.EVENT.register(::onEntityTick)
            WriteDataCallback.EVENT.register(::onWriteData)
            ReadDataCallback.EVENT.register(::onReadData)
        }

        private fun onReadData(entity: Entity, nbt: NbtCompound) {
            if (entity !is MatrixLivingEntity || entity !is LivingEntity) {
                return
            }

            val matrixCompound = nbt.getCompound("MatrixMod")
            val channelingSequences = matrixCompound.getList("ChannelingSequences", NbtElement.COMPOUND_TYPE.toInt())
            for (sequences in channelingSequences) {
                val sequencesCompound = sequences as NbtCompound
                val playerUUID = sequencesCompound.getUuid("PlayerUUID")
                val channelingSequence =
                    sequencesCompound.getList("ChannelingSequence", NbtElement.COMPOUND_TYPE.toInt())
                val magics = channelingSequence
                    .map { it as NbtCompound }
                    .filter { MagicManager.getMagicById(it.getInt("MagicId")) != null }
                    .map {
                        ChannelingMagic(
                            MagicManager.getMagicById(it.getInt("MagicId"))!!,
                            it.getLong("CurrentChannelTime"),
                            it.getLong("ChannelTime"),
                            it.getLong("Cost"),
                            MagicData(tag = it)
                        )
                    }
                    .toMutableList()
                entity.getChannelSequence().compute(playerUUID) { _, _ ->
                    ChannelSequence(
                        entity.world.getPlayerByUuid(nbt.getUuid("UUID")),
                        nbt.getUuid("UUID"),
                        entity,
                        magics
                    )
                }
            }
        }

        private fun onWriteData(entity: Entity, nbt: NbtCompound) {
            if (entity !is MatrixLivingEntity || entity !is LivingEntity) {
                return
            }

            val matrixCompound = nbt.getCompound("MatrixMod")
            val channelingSequences = NbtList()
            for (channelingSequence in entity.getChannelSequence()) {
                val sequencesCompound = NbtCompound()
                sequencesCompound.putUuid("PlayerUUID", channelingSequence.key)
                val channelingSequenceList = NbtList()
                for (magic in channelingSequence.value.magics) {
                    val magicCompound = NbtCompound()
                    magicCompound.putInt("MagicId", magic.magic.name.hashCode())
                    magicCompound.putLong("CurrentChannelTime", magic.currentChannelTime)
                    magicCompound.putLong("ChannelTime", magic.channelTime)
                    magic.data.tag = magicCompound
                    magic.data.writeToTag()
                    channelingSequenceList.add(magicCompound)
                }
                sequencesCompound.put("ChannelingSequence", channelingSequenceList)
                channelingSequences.add(sequencesCompound)
            }
            matrixCompound.put("ChannelingSequences", channelingSequences)
            nbt.put("MatrixMod", matrixCompound)
        }

        private fun onEntityTick(entity: Entity) {
            if (entity !is MatrixLivingEntity) {
                return
            }

            for (sequence in entity.getChannelSequence()) {
                sequence.value.tick()
            }
        }
    }

    var index = 0

    val manaCost
        get() = magics.sumOf { it.cost }

    constructor(target: LivingEntity) : this(null, UUID(0L, 0L), target, mutableListOf())

    fun sequencedBefore(magic: Magic): Boolean {
        val targetIndex = magics.indexOfFirst { it.magic == magic }
        if (targetIndex == -1) {
            return false
        }
        return index < targetIndex
    }

    inline fun <reified T : Magic> sequencedBefore(): Boolean {
        val targetIndex = magics.indexOfFirst { it.magic is T }
        if (targetIndex == -1) {
            return false
        }
        return index < targetIndex
    }

    fun sequencedAfter(magic: Magic): Boolean {
        val targetIndex = magics.indexOfFirst { it.magic == magic }
        if (targetIndex == -1) {
            return false
        }
        return index > targetIndex
    }

    inline fun <reified T : Magic> sequencedAfter(): Boolean {
        val targetIndex = magics.indexOfFirst { it.magic is T }
        if (targetIndex == -1) {
            return false
        }

        return index > targetIndex
    }

    fun channelingMagicCount(): Int {
        return magics.size - index
    }

    fun channelingMagics(): List<ChannelingMagic> {
        return magics.filterIndexed { i, _ -> i >= index }.toList()
    }

    fun castedMagics(): List<ChannelingMagic> {
        return magics.filterIndexed { i, _ -> i < index }.toList()
    }

    fun tick() {
        if (index >= magics.size) {
            // The index is increased after the magic is casted, when we reach there
            // all the magics in the sequence are casted, or there's no more magics left.
            magics.clear()
            locked = false
            index = 0
            return
        }

        val currentChanneling = magics[index]
        if (currentChanneling.currentChannelTime++ >= currentChanneling.channelTime) {
            var player = this.player
            if (player == null) {
                player = target.world.getPlayerByUuid(playerUUID)
            }
            if (player is ServerPlayerEntity?) {
                currentChanneling.magic.cast(player, target, this, currentChanneling.data)
            }
            ++index
        }
    }
}

fun PlayerEntity.getChannelSequence(target: LivingEntity?): ChannelSequence? {
    return ChannelSequence.getChannelSequence(this, target)
}

fun LivingEntity.getChannelSequence(player: PlayerEntity): ChannelSequence? {
    return ChannelSequence.getChannelSequence(player, this)
}

fun LivingEntity.getOrCreateChannelSequence(player: PlayerEntity): ChannelSequence {
    val channelSequence = (this as MatrixLivingEntity).getChannelSequence()
    return channelSequence.computeIfAbsent(player.uuid) { ChannelSequence(player, player.uuid, this, mutableListOf()) }
}

val LivingEntity.allChannelSequences
    get() = (this as MatrixLivingEntity).getChannelSequence()