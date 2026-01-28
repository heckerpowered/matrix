/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.common.magic.channel.ChannelEntry
import heckerpowered.matrix.common.persistent.serialization.seralizer.UUIDSerializer
import heckerpowered.matrix.common.persistent.serialization.seralizer.WorldKeySerializer
import kotlinx.serialization.Serializable
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.*

@Serializable
data class PersistChannelQueue(
    @Serializable(with = WorldKeySerializer::class)
    var world: RegistryKey<World> = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld")),
    @Serializable(with = UUIDSerializer::class)
    var channelerUuid: UUID,
    var isLocked: Boolean = false,
    var active: ChannelEntry? = null,
    val queue: List<ChannelEntry>,
)