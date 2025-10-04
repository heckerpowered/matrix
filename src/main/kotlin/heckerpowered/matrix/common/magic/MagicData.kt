/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import net.minecraft.nbt.NbtCompound

open class MagicData(
    var isSpread: Boolean = false,
    var magicLevel: Int = 1,
    var tag: NbtCompound = NbtCompound(),
) {
    companion object {
        const val IS_SPREAD_TAG = "IsSpread"
        const val MAGIC_LEVEL_KEY = "MagicLevel"
    }

    open fun readFromTag() {
        isSpread = tag.getBoolean(IS_SPREAD_TAG)
        magicLevel = tag.getInt(MAGIC_LEVEL_KEY)
    }

    open fun writeToTag() {
        tag.putBoolean(IS_SPREAD_TAG, isSpread)
        tag.putInt(MAGIC_LEVEL_KEY, magicLevel)
    }
}