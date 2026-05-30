/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.monster.ElderGuardian
import net.minecraft.world.entity.monster.Guardian
import net.minecraft.world.entity.monster.Witch
import net.minecraft.world.entity.monster.illager.Evoker
import net.minecraft.world.entity.monster.warden.Warden

data class SpellProfile(
    val form: SpellForm,
    val rank: SpellRank,
) {
    companion object {
        private val explicitProfiles: Map<EntityType<*>, SpellProfile> = buildMap {
            put(EntityTypes.WITCH, SpellProfile(SpellForm.CASTER, SpellRank.ELITE))
            put(EntityTypes.EVOKER, SpellProfile(SpellForm.CASTER, SpellRank.ELITE))
            put(EntityTypes.ILLUSIONER, SpellProfile(SpellForm.CASTER, SpellRank.ELITE))

            put(EntityTypes.GUARDIAN, SpellProfile(SpellForm.FORMED, SpellRank.ELITE))
            put(EntityTypes.ELDER_GUARDIAN, SpellProfile(SpellForm.FORMED, SpellRank.BOSS))

            put(EntityTypes.WITHER, SpellProfile(SpellForm.FORMED, SpellRank.BOSS))
            put(EntityTypes.ENDER_DRAGON, SpellProfile(SpellForm.FORMED, SpellRank.BOSS))

            put(EntityTypes.WARDEN, SpellProfile(SpellForm.FORMED, SpellRank.CHIMERA))
        }

        private fun inferRank(entity: LivingEntity): SpellRank {
            return when (entity) {
                is EnderDragon, is WitherBoss -> SpellRank.BOSS
                is Warden -> SpellRank.CHIMERA
                is ElderGuardian -> SpellRank.ELITE
                else -> SpellRank.NORMAL
            }
        }

        private fun inferForm(entity: LivingEntity): SpellForm {
            return when (entity) {
                is Witch, is Evoker -> SpellForm.CASTER
                is Guardian -> SpellForm.FORMED
                is WitherBoss, is EnderDragon, is Warden -> SpellForm.FORMED
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