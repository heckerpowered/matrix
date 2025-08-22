/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.effect.MatrixStatusEffects.ANGERED_EFFECT
import net.minecraft.entity.LivingEntity

fun isWardenArmorAngered(entity: LivingEntity): Boolean {
    return entity.getStatusEffect(ANGERED_EFFECT) != null
}