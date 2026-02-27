/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.entity.damage.DamageSource

class DamageSourceEnvelope(val origin: DamageSource, val rawDamage: Float) : DamageSource(origin.typeRegistryEntry, origin.source, origin.attacker)