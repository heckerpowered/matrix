/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

object EntityPolarity {
    // Protection bits
    const val SUPPRESS_DEATH = 1L shl 0
    const val REJECT_DAMAGE = 1L shl 1
    const val INVULNERABLE = 1L shl 2
    const val HEALTH_SPOOF = 1L shl 3
    const val UNTARGETABLE = 1L shl 4
    const val SLOW_IMMUNE = 1L shl 5
    const val FIRE_IMMUNE = 1L shl 6

    // Vulnerability bits
    const val FORCE_DEATH_CHECK = 1L shl 32
    const val ZERO_HEALTH_SPOOF = 1L shl 33
    const val CRIPPLE_MOVEMENT = 1L shl 34
}
