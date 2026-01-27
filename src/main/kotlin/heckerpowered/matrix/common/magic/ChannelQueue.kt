/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.common.persistent.PersistChannelQueue
import heckerpowered.matrix.core.MatrixLivingEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import java.util.*

typealias Channeler = PlayerEntity

class ChannelQueue(
    val channeler: Channeler?,
    var channelerUuid: UUID,
    val target: LivingEntity,
) {
    companion object {
        fun fromPersist(target: LivingEntity, persist: PersistChannelQueue): ChannelQueue {
            val channelQueue = ChannelQueue(
                channeler = target.server?.playerManager?.getPlayer(persist.channelerUuid),
                channelerUuid = persist.channelerUuid,
                target = target,
            )
            channelQueue.isLocked = persist.isLocked
            channelQueue.active = persist.active
            channelQueue.queue.addAll(persist.queue)
            return channelQueue
        }

        fun PlayerEntity.getChannelQueue(target: LivingEntity?): ChannelQueue? {
            return target?.allChannelQueues[uuid]
        }

        fun LivingEntity.getChannelQueue(player: PlayerEntity): ChannelQueue? {
            return player.getChannelQueue(this)
        }

        fun LivingEntity.getOrCreateChannelQueue(player: PlayerEntity): ChannelQueue {
            val channelQueues = allChannelQueues
            return channelQueues.computeIfAbsent(player.uuid) {
                ChannelQueue(player, player.uuid, this)
            }
        }

        val LivingEntity.allChannelQueues
            get() = (this as MatrixLivingEntity).getChannelQueues()
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
        return channelingMagics().any { it.magic is T }
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
        if (++current.currentChannelTime <= current.channelTime) {
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
            queue = queue.toList()
        )
    }
}