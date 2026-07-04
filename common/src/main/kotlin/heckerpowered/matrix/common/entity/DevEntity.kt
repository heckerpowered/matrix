/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity

import heckerpowered.matrix.common.magic.channel.ChannelQueue
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.BossEvent
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.entity.Mob.createMobAttributes
import java.util.UUID

class DevEntity(entityType: EntityType<out DevEntity>, world: Level) : PathfinderMob(entityType, world) {

    override var polarity: Long = 0L
    override var healthSpoofValue: Float = 0.0F
    override val channelQueues: MutableMap<UUID, ChannelQueue> = mutableMapOf()

    private val bossBar = ServerBossEvent(uuid, this.displayName, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_20)

    constructor(world: Level) : this(ModEntityTypes.devEntity, world)

    companion object {
        const val HEALTH_SCALE = 10000000.0

        fun createDevAttributes(): AttributeSupplier.Builder {
            return createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.ARMOR_TOUGHNESS, 2.0)
                .add(Attributes.MAX_HEALTH, 1000.0 * HEALTH_SCALE)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
        }
    }

    override fun hurtServer(level: ServerLevel, source: DamageSource, amount: Float): Boolean {
        return super.hurtServer(level, source, amount)
    }

    override fun getAttributeValue(attribute: Holder<Attribute>): Double {
        if (attribute == Attributes.MAX_HEALTH) {
            return 1000.0 * HEALTH_SCALE
        }
        return super.getAttributeValue(attribute)
    }

    override fun customServerAiStep(level: ServerLevel) {
        super.customServerAiStep(level)

        bossBar.setProgress(health / maxHealth)
    }

    override fun startSeenByPlayer(player: ServerPlayer) {
        super.startSeenByPlayer(player)
        bossBar.addPlayer(player)
    }

    override fun stopSeenByPlayer(player: ServerPlayer) {
        super.stopSeenByPlayer(player)
        bossBar.removePlayer(player)
    }
}