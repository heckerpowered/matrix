/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.mob.*

data class SpellProfile(
    val form: SpellForm,
    val rank: SpellRank,
) {
    companion object {
        private val explicitProfiles: Map<EntityType<*>, SpellProfile> = buildMap {
            put(EntityType.WITCH, SpellProfile(SpellForm.CASTER, SpellRank.ELITE))
            put(EntityType.EVOKER, SpellProfile(SpellForm.CASTER, SpellRank.ELITE))
            put(EntityType.ILLUSIONER, SpellProfile(SpellForm.CASTER, SpellRank.ELITE))

            put(EntityType.GUARDIAN, SpellProfile(SpellForm.FORMED, SpellRank.ELITE))
            put(EntityType.ELDER_GUARDIAN, SpellProfile(SpellForm.FORMED, SpellRank.BOSS))

            put(EntityType.WITHER, SpellProfile(SpellForm.FORMED, SpellRank.BOSS))
            put(EntityType.ENDER_DRAGON, SpellProfile(SpellForm.FORMED, SpellRank.BOSS))

            put(EntityType.WARDEN, SpellProfile(SpellForm.FORMED, SpellRank.CHIMERA))
        }

        private fun inferRank(entity: LivingEntity): SpellRank {
            return when (entity) {
                is EnderDragonEntity, is WitherEntity -> SpellRank.BOSS
                is WardenEntity -> SpellRank.CHIMERA
                is ElderGuardianEntity -> SpellRank.ELITE
                else -> SpellRank.NORMAL
            }
        }

        private fun inferForm(entity: LivingEntity): SpellForm {
            return when (entity) {
                is WitchEntity, is EvokerEntity -> SpellForm.CASTER
                is GuardianEntity -> SpellForm.FORMED
                is WitherEntity, is EnderDragonEntity, is WardenEntity -> SpellForm.FORMED
                else -> SpellForm.NATURAL
            }
        }

        fun getProfile(entity: LivingEntity): SpellProfile {
            explicitProfiles[entity.type]?.let { return it }

            val rank = inferRank(entity)
            val form = inferForm(entity)

            return SpellProfile(form = form, rank = rank)
        }
    }

    val effectiveResistance by lazy {
        val resistance = rank.resistance + form.resistance

        if (form != SpellForm.CASTER) {
            return@lazy resistance
        }

        when (rank) {
            SpellRank.BOSS -> rank.resistance + 2.0
            else -> rank.resistance + 6.0
        }
    }
}