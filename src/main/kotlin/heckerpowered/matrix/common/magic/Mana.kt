/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

@JvmInline
value class Mana(val amount: Long) {
    companion object {
        val Number.mana: Mana
            get() = Mana(toLong())
    }
}