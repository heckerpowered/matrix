/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.gameplay.state

import heckerpowered.matrix.common.magic.resource.Mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana

object ClientManaState {
    var current: Mana = 0.mana
    var maxMana: Mana = 0.mana
}