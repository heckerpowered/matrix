/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import com.mojang.serialization.Codec
import heckerpowered.matrix.Matrix
import net.minecraft.core.UUIDUtil
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import java.util.UUID

/**
 * World-persistent overclock rates keyed by player UUID, restored from the pre-migration jar.
 *
 * 26.2 note: [SavedData] moved from the `writeNbt`/factory model to codec-based
 * [SavedDataType]s; the codec reproduces the pre-migration tag layout
 * (`players` -> uuid -> [OverclockData]) inside the new `data/matrix/overclock_state.dat`
 * location (saved data files are namespaced directories now, so the old flat
 * `matrix_overclock_state.dat` is no longer reachable through the vanilla storage API).
 */
class OverclockState : SavedData() {
    val overclockData: MutableMap<UUID, OverclockData> = mutableMapOf()

    companion object {
        val CODEC: Codec<OverclockState> =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, OverclockData.CODEC)
                .optionalFieldOf("players", emptyMap())
                .xmap(
                    { players -> OverclockState().also { it.overclockData.putAll(players) } },
                    { state -> state.overclockData },
                )
                .codec()

        val TYPE: SavedDataType<OverclockState> = SavedDataType(
            Matrix.identifier("overclock_state"),
            { OverclockState() },
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE,
        )

        fun getServerState(server: MinecraftServer): OverclockState {
            val state = server.overworld().dataStorage.computeIfAbsent(TYPE)
            // Mirrors the pre-migration implementation: mark dirty on every access so the
            // mutable per-player entries handed out below always get persisted.
            state.setDirty()
            return state
        }

        fun getPlayerState(player: LivingEntity): OverclockData {
            val server = requireNotNull(player.level().server) { "overclock state is server-side only" }
            return getServerState(server).overclockData.computeIfAbsent(player.uuid) { OverclockData() }
        }
    }
}

/** The player's magic overclock rate in `1.0..10.0`; scales magic strength. */
val ServerPlayer.magicClock: Double get() = OverclockState.getPlayerState(this).magicOverclock

/** The player's mana overclock rate in `1.0..10.0`; scales max mana. */
val ServerPlayer.manaClock: Double get() = OverclockState.getPlayerState(this).manaOverclock
