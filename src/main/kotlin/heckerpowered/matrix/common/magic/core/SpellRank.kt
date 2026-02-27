/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

enum class SpellRank(val resistance: Double) {
    NORMAL(.0),
    ELITE(1.0),
    BOSS(3.0),
    CHIMERA(3.0)
}