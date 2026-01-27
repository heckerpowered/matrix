/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.common.enchantment.MatrixEnchantments
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.event.ReadDataCallback
import heckerpowered.matrix.common.event.WriteDataCallback
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.magic.ChannelQueue.Companion.allChannelQueues
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.Mana.Companion.plus
import heckerpowered.matrix.common.magic.spell.*
import heckerpowered.matrix.common.network.ChannelMagicPayload
import heckerpowered.matrix.common.network.SyncManaPayload
import heckerpowered.matrix.common.persistent.*
import heckerpowered.matrix.common.persistent.serialization.NbtCodec
import heckerpowered.matrix.common.persistent.serialization.seralizer.UUIDSerializer
import heckerpowered.matrix.core.MatrixLivingEntity
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.modules.plus
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

object MagicManager {
    private val magics = mutableMapOf<UUID, Magic>()
    private var registeredMagics = mutableListOf<Magic>()
    private var snapshot = Collections.unmodifiableList(registeredMagics)

    fun getRegisteredMagics(): List<Magic> = snapshot

    fun getMagicByUuid(id: UUID): Magic? = magics[id]

    fun registerMagic(magic: Magic) {
        val uuid = magic.definition.uuid
        require(magics.putIfAbsent(uuid, magic) == null) { "Duplicate Magic: $uuid" }
        registeredMagics.add(magic)

        if (magic is MagicDataSpecification) {
            ExecutionPayload.serializationModule += magic.serializerModule()
        }
    }

    private fun registerBuiltinMagics() {
        registerMagic(TargetPositioningMagic)
        registerMagic(DecisiveStrikeMagic)
        registerMagic(HealthStealMagic)
        registerMagic(ManaOverloadMagic)
        registerMagic(ExplosionMagic)
        registerMagic(KillMagic)
        registerMagic(SculkCatalystMagic)
        registerMagic(IgniteMagic)
        registerMagic(BreakingBadMagic)
        registerMagic(CrippleMovementMagic)
        registerMagic(MemoryWipeMagic)
        registerMagic(SpreadMagic)
        registerMagic(SystemCrashMagic)
        registerMagic(LightningBoltMagic)
        registerMagic(TeleportMagic)
        registerMagic(ArmorPenetrationMagic)
        registerMagic(SonicBoomMagic)
        registerMagic(BruteForceMagic)
        registerMagic(AttractMagic)
        registerMagic(LevitationMagic)
        registerMagic(AbsolvriftMagic)
    }

    fun getMagics(player: PlayerEntity): List<Magic> {
        val itemStack = player.getEquippedStack(EquipmentSlot.HEAD)
        val item = itemStack.item
        if (item !is WizardHelmet) {
            return listOf()
        }

        return item.getMagics(player, itemStack)
    }

    fun onInitialize() {
        registerBuiltinMagics()
        EntityTickCallback.EVENT.register(::onEntityTick)
        WriteDataCallback.EVENT.register(::onWriteData)
        ReadDataCallback.EVENT.register(::onReadData)
        ServerTickEvents.END_SERVER_TICK.register(::onServerTick)
    }

    private fun onWriteData(entity: Entity, compound: NbtCompound) {
        if (entity !is MatrixLivingEntity || entity !is LivingEntity) {
            return
        }

        val matrixCompound = compound.getCompound("MatrixMod")
        val channelQueues = entity
            .allChannelQueues
            .mapValues { it.value.toPersist() }
        val encodedQueues = NbtCodec.encode(
            channelQueues,
            NbtCodec(ExecutionPayload.serializationModule),
            serializer = MapSerializer(UUIDSerializer, PersistChannelQueue.serializer())
        )
        matrixCompound.put("ChannelQueues", encodedQueues)
        compound.put("MatrixMod", matrixCompound)
    }

    private fun onReadData(entity: Entity, compound: NbtCompound) {
        if (entity !is MatrixLivingEntity || entity !is LivingEntity) {
            return
        }

        val matrixCompound = compound.getCompound("MatrixMod")
        val encodedQueues = matrixCompound.getCompound("ChannelQueues")
        val decodedQueues = NbtCodec.decode<Map<UUID, PersistChannelQueue>>(
            encodedQueues,
            NbtCodec(ExecutionPayload.serializationModule),
            deserializer = MapSerializer(UUIDSerializer, PersistChannelQueue.serializer())
        )
        decodedQueues.forEach { (uuid, persistQueue) ->
            val channelQueue = ChannelQueue.fromPersist(entity, persistQueue)
            entity.allChannelQueues[uuid] = channelQueue

            // val channeler = channelQueue.channeler as? ServerPlayerEntity ?: return@forEach
            // channelQueue.channelingMagics().forEach { channelingMagic ->
            //     ServerPlayNetworking.send(
            //         channeler, ChannelMagicPayload(
            //             channelingMagic.magic.definition.uuid,
            //             entity.id,
            //             channelingMagic.channelTime,
            //             channelingMagic.currentChannelTime
            //         )
            //     )
            // }
        }
    }

    private fun onServerTick(minecraftServer: MinecraftServer) {
        minecraftServer.playerManager.playerList.forEach {
            if (it.isInfiniteMana) {
                it.mana = it.maxMana
            }
        }
        if (minecraftServer.ticks % 20 != 0) {
            return
        }

        minecraftServer.playerManager.playerList.forEach {
            val wizardHelmet = it.wizardHelmet
            val item = wizardHelmet.item
            if (item is WizardHelmet) {
                it.maxMana = item.getMaxMana(it, wizardHelmet).mana
            } else {
                it.maxMana = .0.mana
            }

            var manaRegen = 1.0
            val manaRegenerationLevel = wizardHelmet.getEnchantmentLevel(MatrixEnchantments.MANA_REGENERATION_ENCHANTMENT_KEY)
            if (manaRegenerationLevel > 0) {
                manaRegen += manaRegen * (manaRegenerationLevel * 0.3)
            }
            it.mana += manaRegen.mana
            ServerPlayNetworking.send(it, SyncManaPayload(it.mana.amount, it.maxMana.amount))
        }
    }

    private fun onEntityTick(entity: Entity) {
        if (entity !is MatrixLivingEntity) {
            return
        }

        for (queue in entity.getChannelQueues().values) {
            val magic = queue.tick() ?: continue
            var channeler = queue.channeler
            val target = queue.target
            if (channeler == null) {
                channeler = target.world.getPlayerByUuid(queue.channelerUuid)
            }
            if (channeler is ServerPlayerEntity?) {
                magic.magic.cast(channeler, target, queue, magic.data)
            }
        }
    }

    @JvmStatic
    fun onEntityTracked(player: ServerPlayerEntity, entity: Entity) {
        if (entity !is LivingEntity) {
            return
        }

        val channelQueue = entity.allChannelQueues[player.uuid] ?: return
        channelQueue.channelingMagics().forEach { channelingMagic ->
            ServerPlayNetworking.send(
                player, ChannelMagicPayload(
                    channelingMagic.magic.definition.uuid,
                    entity.id,
                    channelingMagic.channelTime,
                    channelingMagic.currentChannelTime
                )
            )
        }
    }
}