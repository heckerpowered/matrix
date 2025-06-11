package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LIGHTNING_STRIKE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.core.extensions.SequenceExtensions.consumeWhile
import heckerpowered.matrix.core.minus
import heckerpowered.matrix.core.utility.EntitySearch.getAdjacentEntities
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import kotlin.math.floor

/**
 *
 */
object LightningStrikeEnchantment {
    fun onInitialize() {
        LivingHurtCallback.EVENT.register(::onHurt)
    }

    fun onHurt(event: DamageAccumulator): ActionResult {
        val attacker = event.attacker ?: return ActionResult.PASS
        val serverWorld = attacker.world as? ServerWorld ?: return ActionResult.PASS

        val registryManager = attacker.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val enchantmentEntry = registryWrapper.getOrThrow(LIGHTNING_STRIKE_ENCHANTMENT_KEY)
        val enchantmentLevel = attacker.handItems.sumOf { EnchantmentHelper.getLevel(enchantmentEntry, it) }
        if (enchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        val damageSource = event.target.damageSources.lightningBolt()
        var previousEntity = attacker
        event.target.getAdjacentEntities(8.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != attacker }
            .consumeWhile(5) { it.damage(damageSource, event.baseDamage.toFloat()) }
            .forEach { entity ->
                val startPosition = previousEntity.pos
                val endPosition = entity.pos
                val direction = endPosition - startPosition

                val step = floor(direction.length() * 10).toInt()
                val normalizedDirection = direction.normalize()
                for (i in 1..step) {
                    val currentPosition = startPosition.add(normalizedDirection.multiply(i.toDouble() * 0.1))
                    serverWorld.spawnParticles(ParticleTypes.SMOKE, currentPosition.x, currentPosition.y, currentPosition.z, 1, 0.0, 0.0, 0.0, 0.0)
                }

                previousEntity = entity
            }
        return ActionResult.PASS
    }
}