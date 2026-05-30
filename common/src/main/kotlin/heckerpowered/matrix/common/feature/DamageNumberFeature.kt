/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.feature

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.network.ClientboundDamageNumberPayload
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypeTags
import heckerpowered.matrix.core.extension.sample
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import org.joml.Vector3f

object DamageNumberFeature : DamageOutcomeRule {
    init {
        RuleRegistry.register<DamageOutcomeRule>(this)
        // LivingHealCallback.EVENT.register(::onLivingHeal)
        // TODO: Implement heal outcome rule
    }

    fun onInitialize() {
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val target = context.target
        if (target.level() !is ServerLevel) return

        val position = target.boundingBox.sample(context.realizedDamage.toDouble(), 0.1)
        val color = getColorForDamageSource(context.source)
        val payload = ClientboundDamageNumberPayload(context.realizedDamage, position, color)
        target.level().server?.playerList?.players?.forEach { player ->
            ServerPlayNetworking.send(player, payload)
        }
    }

    /**
    private fun onLivingHeal(event: LivingHealEvent): ActionResult {
    val target = event.entity
    if (target.world !is ServerWorld) return ActionResult.PASS

    val position = target.boundingBox.sample(event.amount.toDouble(), 0.1)
    val color = Vector3f(25f, 255f, 25f)
    val payload = ClientboundDamageNumberPayload(event.amount, position, color)
    target.world.server?.playerManager?.playerList?.forEach { player ->
    ServerPlayNetworking.send(player, payload)
    }

    return ActionResult.PASS
    }
     */

    fun getColorForDamageSource(damage: DamageSource): Vector3f {
        return when {
            damage.`is`(MatrixDamageTypeTags.magic) ->
                Vector3f(25f, 128f, 255f)

            damage.`is`(DamageTypes.MAGIC) ||
                    damage.`is`(DamageTypes.INDIRECT_MAGIC) ->
                Vector3f(25f, 25f, 128f)

            damage.`is`(DamageTypeTags.IS_FIRE) ||
                    damage.`is`(DamageTypes.LAVA) ||
                    damage.`is`(DamageTypes.HOT_FLOOR) ->
                Vector3f(255f, 100f, 25f)

            damage.`is`(DamageTypes.EXPLOSION) ||
                    damage.`is`(DamageTypes.PLAYER_EXPLOSION) ||
                    damage.`is`(DamageTypes.FIREWORKS) ->
                Vector3f(255f, 170f, 60f)

            damage.`is`(DamageTypes.LIGHTNING_BOLT) ||
                    damage.`is`(DamageTypes.SONIC_BOOM) ||
                    damage.`is`(DamageTypes.WIND_CHARGE) ->
                Vector3f(160f, 80f, 255f)

            damage.`is`(DamageTypes.STARVE) ||
                    damage.`is`(DamageTypes.DRY_OUT) ->
                Vector3f(255f, 230f, 80f)

            damage.`is`(DamageTypes.THORNS) ||
                    damage.`is`(DamageTypes.CACTUS) ||
                    damage.`is`(DamageTypes.SWEET_BERRY_BUSH) ->
                Vector3f(80f, 200f, 80f)

            else -> Vector3f(255f, 255f, 255f)
        }
    }
}