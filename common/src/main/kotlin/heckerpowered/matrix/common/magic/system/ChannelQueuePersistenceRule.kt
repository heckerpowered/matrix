/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

import heckerpowered.matrix.common.entity.rule.LivingLoadContext
import heckerpowered.matrix.common.entity.rule.LivingPersistenceRule
import heckerpowered.matrix.common.entity.rule.LivingSaveContext
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.persistent.PersistChannelQueue
import heckerpowered.matrix.common.persistent.QueueLoadedContext
import heckerpowered.matrix.common.persistent.QueueLoadedRule
import heckerpowered.matrix.common.persistent.serialization.NbtCodec
import heckerpowered.matrix.common.persistent.serialization.seralizer.UUIDSerializer
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.all
import heckerpowered.matrix.common.rule.register
import kotlinx.serialization.builtins.MapSerializer
import net.minecraft.nbt.CompoundTag
import java.util.*
import kotlin.jvm.optionals.getOrNull

object ChannelQueuePersistenceRule : LivingPersistenceRule {
    private const val CHANNEL_QUEUE_KEY = "matrix_channel_queues"

    init {
        RuleRegistry.register<LivingPersistenceRule>(this)
    }

    fun onInitialize() {
    }

    override fun save(context: LivingSaveContext) {
        val entity = context.entity
        val output = context.output

        val channelQueues = entity.channelQueues
            .mapValues { it.value.toPersist() }

        val encodedQueues = NbtCodec.encode(
            channelQueues,
            NbtCodec(ExecutionPayload.serializationModule),
            serializer = MapSerializer(UUIDSerializer, PersistChannelQueue.serializer())
        )
        output.store(CHANNEL_QUEUE_KEY, CompoundTag.CODEC, encodedQueues)
    }

    override fun load(context: LivingLoadContext) {
        val entity = context.entity
        val input = context.input

        val encodedQueues = input.read(CHANNEL_QUEUE_KEY, CompoundTag.CODEC).getOrNull() ?: return
        val decodedQueues = NbtCodec.decode<Map<UUID, PersistChannelQueue>>(
            encodedQueues,
            NbtCodec(ExecutionPayload.serializationModule),
            deserializer = MapSerializer(UUIDSerializer, PersistChannelQueue.serializer())
        )

        val server = entity.level().server
        for ((uuid, persistQueue) in decodedQueues) {
            val channelQueue = ChannelQueue.fromPersist(server, entity, persistQueue)
            entity.channelQueues[uuid] = channelQueue

            val context = QueueLoadedContext(entity, uuid, channelQueue)
            for (rule in RuleRegistry.all<QueueLoadedRule>()) {
                rule.onLoaded(context)
            }
        }
    }
}
