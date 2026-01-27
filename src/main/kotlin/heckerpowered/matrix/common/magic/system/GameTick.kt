/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.system

@JvmInline
value class GameTick(val ticks: Long) {
    companion object {
        val Number.ticks: GameTick
            get() = GameTick(this.toLong())
    }
}