/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.persistent.PersistChannelQueue
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import java.util.*

typealias Channeler = Player

class ChannelQueue(
    val channeler: Channeler?,
    val channelerUuid: UUID,
    val target: LivingEntity,
) {
    companion object {
        fun fromPersist(minecraftServer: MinecraftServer?, target: LivingEntity, persist: PersistChannelQueue): ChannelQueue {
            val world = minecraftServer?.getLevel(persist.level) ?: target.level()
            val channelQueue = ChannelQueue(
                channeler = world.getEntity(persist.channelerUuid) as? Player,
                channelerUuid = persist.channelerUuid,
                target = target,
            )
            channelQueue.isLocked = persist.isLocked
            channelQueue.active = persist.active
            channelQueue.queue.addAll(persist.queue)
            return channelQueue
        }

        fun Player.getChannelQueue(target: LivingEntity?): ChannelQueue? {
            return target?.channelQueues[uuid]
        }

        fun LivingEntity.getChannelQueue(player: Player): ChannelQueue? {
            return player.getChannelQueue(this)
        }

        fun LivingEntity.getOrCreateChannelQueue(player: Player): ChannelQueue {
            val channelQueues = channelQueues
            return channelQueues.computeIfAbsent(player.uuid) {
                ChannelQueue(player, player.uuid, this)
            }
        }
    }

    var isLocked: Boolean = false

    var active: ChannelEntry? = null
    val queue: ArrayDeque<ChannelEntry> = ArrayDeque()

    val isEmpty: Boolean
        get() = active == null && queue.isEmpty()
    val isChanneling: Boolean
        get() = active != null
    val channelingMagicCount: Int
        get() = queue.size + if (active != null) 1 else 0
    val queuedMagicCount: Long
        get() = queue.size.toLong()

    fun clear() {
        active = null
        queue.clear()
    }

    fun enqueue(pending: ChannelEntry) {
        if (active == null) {
            active = pending
            return
        }
        queue.addLast(pending)
    }

    fun channelingMagics(): List<ChannelEntry> = buildList {
        active?.let(::add)
        addAll(queue)
    }

    inline fun <reified T : Magic> contains(): Boolean {
        return active?.magic is T || queue.any { it.magic is T }
    }

    /**
     * Advance the currently active channel by one tick.
     *
     * If there is no active channeling, returns null immediately.
     * If the active channeling is still in progress, increments its progress and returns null.
     * If the active channeling has finished, removes it from active, promotes the next
     * pending entry from the queue (if any), and returns the finished channeling.
     *
     * @return the finished [ChannelEntry] when one just completed in this tick,
     *         or null if none completed.
     */
    fun tick(): ChannelEntry? {
        if (isEmpty) {
            isLocked = false
        }
        val current = active ?: return null
        if (++current.currentChannelTime < current.channelTime) {
            return null
        }

        active = queue.pollFirst()
        return current
    }

    fun toPersist(): PersistChannelQueue {
        return PersistChannelQueue(
            channelerUuid = channelerUuid,
            isLocked = isLocked,
            active = active,
            queue = queue.toList(),
            level = (channeler ?: target).level().dimension()
        )
    }
}

fun ChannelQueue.resolveCaster(): CasterContext {
    return when (val channeler = channeler) {
        is ServerPlayer -> PlayerCaster(channeler)
        is LivingEntity -> EntityCaster(channeler)
        else -> DetachedCaster(target.level() as ServerLevel, channelerUuid)
    }
}