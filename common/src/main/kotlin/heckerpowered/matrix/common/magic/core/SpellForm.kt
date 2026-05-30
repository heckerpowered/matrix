/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

enum class SpellForm(val resistance: Double) {
    NATURAL(0.0),
    BOUND(1.0),
    FORMED(2.0),
    CASTER(6.0),
    SHELLED(1.0);
}