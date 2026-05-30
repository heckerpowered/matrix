/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

interface DamageContext {
    val target: LivingEntity
    val source: DamageSource
    val rawDamage: Float
}

val DamageContext.attacker: Entity?
    get() = source.entity

val DamageContext.directEntity: Entity?
    get() = source.directEntity

fun DamageContext.attackerAsLiving(): LivingEntity? {
    return attacker as? LivingEntity
}