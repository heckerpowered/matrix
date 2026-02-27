/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extension

import heckerpowered.matrix.extension.DamageSourceExtension
import net.minecraft.entity.damage.DamageSource

var DamageSource.isAdditionalDamage: Boolean
    get() = (this as DamageSourceExtension).`matrix$isAdditionalDamage`()
    set(value) {
        (this as DamageSourceExtension).`matrix$setAdditionalDamage`(value)
    }
