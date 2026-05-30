/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.common.magic.channel.ChannelEntry
import heckerpowered.matrix.common.persistent.serialization.seralizer.LevelKeySerializer
import heckerpowered.matrix.common.persistent.serialization.seralizer.UUIDSerializer
import kotlinx.serialization.Serializable
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import java.util.*

@Serializable
data class PersistChannelQueue(
    @Serializable(with = LevelKeySerializer::class)
    var level: ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, Identifier.withDefaultNamespace("overworld")),
    @Serializable(with = UUIDSerializer::class)
    var channelerUuid: UUID,
    var isLocked: Boolean = false,
    var active: ChannelEntry? = null,
    val queue: List<ChannelEntry>,
)