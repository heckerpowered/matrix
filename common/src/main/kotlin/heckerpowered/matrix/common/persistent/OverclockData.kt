/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * Per-player overclock rates adjusted from the HUD (N/M keys), both in `1.0..10.0`.
 *
 * Restored from the pre-migration jar: the mana rate scales the player's max mana and the
 * magic rate scales magic strength (currently the explosion power of
 * [heckerpowered.matrix.common.magic.spell.ExplosionMagic]).
 */
data class OverclockData(
    var manaOverclock: Double = 1.0,
    var magicOverclock: Double = 1.0,
) {
    companion object {
        /** Field names match the pre-migration NBT layout (`ManaOverclock`/`MagicOverclock`). */
        val CODEC: Codec<OverclockData> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("ManaOverclock", 1.0).forGetter(OverclockData::manaOverclock),
                Codec.DOUBLE.optionalFieldOf("MagicOverclock", 1.0).forGetter(OverclockData::magicOverclock),
            ).apply(instance) { mana, magic -> OverclockData(mana, magic) }
        }
    }
}
