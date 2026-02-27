/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

interface DamageContext {
    val target: LivingEntity
    val source: DamageSource
    val rawDamage: Float
}

val DamageContext.attacker: Entity?
    get() = source.attacker

val DamageContext.directEntity: Entity?
    get() = source.source

fun DamageContext.attackerAsLiving(): LivingEntity? {
    return source.attacker as? LivingEntity
}