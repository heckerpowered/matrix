/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.common.effect.ModMobEffects.ArmorPenetration
import heckerpowered.matrix.common.effect.ModMobEffects.CrippleMovement
import heckerpowered.matrix.common.effect.ModMobEffects.Exposed
import heckerpowered.matrix.common.entity.MagicLightningBolt.LightningType.*
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.ExecutionPolicy
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.spell.CrippleMovementMagic
import heckerpowered.matrix.common.magic.spell.ExplosionMagic.damageCalculator
import heckerpowered.matrix.common.magic.spell.LightningBoltMagic
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extension.damage
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import java.util.*

class MagicLightningBolt(entityType: EntityType<MagicLightningBolt>, level: Level) : LightningBolt(entityType, level) {

    var lightningType = NORMAL

    /**
     * Represents different lightning colors used in bloom effects.
     *
     * All colors (except [BLACK]) have been adjusted so that their
     * perceived brightness (NIST relative luminance) is approximately
     * equal to [NORMAL]. This ensures that during bloom rendering,
     * each color contributes with a consistent brightness level while
     * still preserving its hue and saturation.
     *
     * [BLACK] remains absolute dark with zero luminance.
     */
    enum class LightningType(val color: Color) {
        NORMAL(Color(114, 114, 128, 255)),
        RED(Color(228, 24, 24, 255)),
        ORANGE(Color(181, 90, 24, 255)),
        YELLOW(Color(119, 119, 24, 255)),
        GREEN(Color(24, 133, 24, 255)),
        CYAN(Color(24, 128, 128, 255)),
        BLUE(Color(92, 92, 255, 255)),
        PURPLE(Color(200, 24, 200, 255)),
        BLACK(Color(0, 0, 0, 255))
    }

    constructor(level: Level) : this(ModEntityTypes.MAGIC_LIGHTNING_ENTITY, level)

    private fun onStruckByLightning(entity: Entity) {
        entity.remainingFireTicks = entity.remainingFireTicks + 1
        if (entity.remainingFireTicks == 0) {
            entity.igniteForSeconds(8.0f)
        }

        val channeler = this.cause
        val damageSource = if (channeler != null) {
            channeler.level().damageSources().source(MatrixDamageTypes.magic, channeler)
        } else {
            damageSources().generic()
        }
        when (lightningType) {
            NORMAL -> performNormalStrike(entity)
            RED -> entity.damage(damageSource, 20.0F)
            ORANGE -> {
                if (entity is LivingEntity) {
                    entity.addEffect(MobEffectInstance(ArmorPenetration, 20 * 10, 4))
                }
                entity.damage(damageSource, 5.0F)
            }

            YELLOW -> {
                channeler?.apply {
                    attackStrengthTicker = Int.MAX_VALUE
                    swing(usedItemHand)
                    attack(entity)
                    crit(entity)
                    magicCrit(entity)
                }
                entity.damage(damageSource, 5.0F)
                if (entity is LivingEntity) {
                    entity.addEffect(MobEffectInstance(MobEffects.GLOWING, 20 * 10, 0))
                }
            }

            GREEN -> {
                channeler?.heal(2F)
                entity.damage(damageSource, 5.0F)
                if (entity is LivingEntity) {
                    entity.addEffect(MobEffectInstance(MobEffects.POISON, 20 * 10, 4))
                }
            }

            CYAN -> {
                if (entity is LivingEntity) {
                    if (entity.hasEffect(CrippleMovement)) {
                        entity.addEffect(MobEffectInstance(Exposed, 20 * 10, 0))
                    }
                    if (channeler == null) {
                        entity.addEffect(MobEffectInstance(CrippleMovement, 20 * 10, 4))
                    } else {
                        val invocation = MagicInvocation.fromEntity(channeler, entity)
                        val attempt = ExecutionPolicy(costMana = false)
                        ChannelExecutor.channel(CrippleMovementMagic, invocation, attempt)
                    }
                }
            }

            BLUE -> {
                if (channeler == null) {
                    entity.damage(damageSource, 5.0F)
                    return
                }

                for (target in entity.level().getEntities(entity, entity.boundingBox.inflate(6.0))) {
                    if (target !is LivingEntity) {
                        continue
                    }

                    val invocation = MagicInvocation.fromEntity(channeler, target)
                    ChannelExecutor.channel(LightningBoltMagic, invocation)
                }
            }

            PURPLE -> {
                entity.damage(damageSource, 5F)
                entity.level().explode(entity, damageSource, damageCalculator, entity.x, entity.y, entity.z, 1.0F, false, Level.ExplosionInteraction.MOB)
                // TODO: add status effect
            }

            BLACK -> {
                (entity.level() as? ServerLevel)?.let(entity::kill)
            }
        }
    }

    private fun damageSource(): DamageSource {
        val cause = this.cause
        return if (cause != null) {
            damageSources().source(MatrixDamageTypes.magic, cause)
        } else {
            damageSources().lightningBolt()
        }
    }

    private fun performNormalStrike(entity: Entity) {
        val level = this.level() as? ServerLevel ?: return
        entity.hurtServer(level, damageSource(), 5.0F)
    }

    private fun performRedStrike(entity: Entity) {
        val level = this.level() as? ServerLevel ?: return
        entity.hurtServer(level, damageSource(), 20.0F)
    }

    private fun performOrangeStrike(entity: Entity) {
        if (entity is LivingEntity) {
            entity.addEffect(MobEffectInstance(ArmorPenetration, 20 * 10, 4))
        }
        entity.damage(damageSource(), 5.0F)
    }

    override fun recreateFromPacket(packet: ClientboundAddEntityPacket) {
        super.recreateFromPacket(packet)

        val lightningTypeOrdinal = packet.data
        val lightningTypes = LightningType.entries.toTypedArray()
        if (lightningTypeOrdinal in lightningTypes.indices) {
            lightningType = lightningTypes[lightningTypeOrdinal]
        }
    }
}
