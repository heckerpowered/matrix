/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import kotlinx.serialization.Serializable

@Serializable
data class ManaData(var mana: Double, var maxMana: Double, var isInfinite: Boolean = false)
