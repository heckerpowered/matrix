package heckerpowered.matrix.common

import heckerpowered.matrix.common.effect.WitherArmorChargedEffect
import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.common.enchantment.*
import heckerpowered.matrix.common.item.MatrixComponents
import heckerpowered.matrix.common.item.WizardHelmetWarpDancer
import heckerpowered.matrix.common.magics.MagicAvailableStatus
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.queueSize
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.lerp
import heckerpowered.matrix.core.mana
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.mob.WardenEntity
import net.minecraft.entity.mob.WitchEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.Difficulty
import kotlin.math.floor

/**
 * Magic can be cast by players, targeting a single living entity, and casting requires channeling; when
 * channeling multiple magics on the same target, they are channeled in sequence. There are limited number
 * of magics that can be channeled at once, magics that channeled at the same time are called channeling sequence.
 *
 * The phase between the player uses the magic and when it takes effect is called "channeling", when a
 * magic takes effect, it is called "casting".
 *
 * @see ChannelSequence
 */
abstract class Magic(
    /**
     * The name of the magic.
     */
    val name: Text,

    /**
     * The cost of the magic.
     */
    private val cost: Long,

    /**
     * The description of the magic.
     */
    val description: Text,

    /**
     * The time it takes for the magic to channel, in ticks.
     */
    private val channelTime: Long = 10,
) {
    /**
     * Called when the magic is casting.
     */
    open fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {}
    open fun channel(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence) {
        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        if (player.wizardHelmet.getEnchantmentLevel(queueMastery) > 0 &&
            sequence.channelingMagicCount().toLong() == player.queueSize
        ) {
            sequence.locked = true
        }

        if (player.bloodPactActive) {
            WitherArmorChargedEffect.onEntityTick(player)
            if (player.wizardHelmet.getEnchantmentLevel(peakOverdrive) > 0) {
                channelPeakOverdrive(player, target, sequence)
            }
        }
    }

    protected open fun channelPeakOverdrive(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence) {
        val wizardHelmet = player.wizardHelmet
        val currentLoad = wizardHelmet.getOrDefault(MatrixComponents.load, .0)
        player.wizardHelmet.set(MatrixComponents.load, currentLoad + 1)
    }

    open fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        if (!checkMana(player, target, sequence)) {
            return MagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH
        }

        if (target == null && !mayChannelWithoutTarget(player)) {
            return MagicAvailableStatus.TARGET_MISSING
        }

        if (checkChannelSequenceIsFull(player, target, sequence)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_FULL
        }

        if (checkChannelSequenceIsLocked(player, target, sequence)) {
            return MagicAvailableStatus.CHANNEL_QUEUE_LOCKED
        }

        return MagicAvailableStatus.AVAILABLE
    }

    protected open fun checkChannelSequenceIsLocked(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Boolean {
        return sequence?.locked ?: false
    }

    /**
     * Check whether the channel sequence is full.
     */
    protected open fun checkChannelSequenceIsFull(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Boolean {
        if (sequence == null) {
            return false
        }

        if (sequence.channelingMagicCount() >= player.queueSize) {
            return true
        }

        return false
    }

    /**
     * Whether the magic can be channeled without a target.
     */
    protected open fun mayChannelWithoutTarget(player: PlayerEntity): Boolean {
        return false
    }

    /**
     * Check whether the player has enough mana to channel the magic
     */
    protected open fun checkMana(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Boolean {
        var mana = player.mana

        val cost = getCost(player, target, sequence)
        if (player.bloodPactActive) {
            val convertRatio = getBloodPactConvertRatio(player, target, sequence)
            val healthToMana = player.health * convertRatio // 1 health = 5 mana
            mana += healthToMana
        }

        if (cost > mana) {
            return false
        }

        return true
    }

    /**
     * Gets the base mana cost of this magic; this value is not necessarily the value needed to channel the magic,
     * but can be used to compare whether the mana required has increased or decreased.
     */
    fun getNormalCost(): Long {
        return cost
    }

    /**
     * Gets the mana needed to channel this magic.
     */
    open fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Long {
        var cost = getNormalCost().toDouble()
        when (target) {
            is WitchEntity -> cost *= 1.85
            is WitherEntity, is EnderDragonEntity -> cost *= 2
            is WardenEntity -> cost *= 3
        }

        val difficulty = player.world.difficulty!!
        when (difficulty) {
            Difficulty.PEACEFUL -> cost *= 0.5
            Difficulty.EASY -> cost *= 0.6
            Difficulty.NORMAL -> cost *= 0.7
            Difficulty.HARD -> cost *= 1
        }

        val entry = player.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(proximatePropagationEnchantmentKey)
        if (target != null && EnchantmentHelper.getLevel(entry, player.wizardHelmet) > 0) {
            val distance = player.squaredDistanceTo(target)
            val maxDistance = 12.0 * 12.0
            val minDistance = 4.0 * 4.0
            val ratio = 1 - distance.inverseLerp(minDistance..maxDistance).coerceIn(.0..1.0)
            cost -= cost * ratio.lerp(.0..0.35)
        }

        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        if (player.wizardHelmet.getEnchantmentLevel(queueMastery) > 0 &&
            sequence?.channelingMagicCount()?.toLong() == player.queueSize - 1
        ) {
            cost *= 0.5
        }

        return floor(cost).toLong().coerceAtLeast(0)
    }

    /**
     * Gets the time it takes for the magic to channel, in ticks; this value is not necessarily the value needed to
     * channel the magic, but can be used to compare whether the channel time has increased or decreased.
     */
    fun getNormalChannelTime(): Long {
        return channelTime
    }

    /**
     * Gets the time it takes for the magic to channel, in ticks.
     */
    open fun getChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence?): Long {
        var basicChannelTime = channelTime.toDouble()
        var channelSpeed = 1.0

        // Magic Queue: +30% channel speed for the second magic in a queue.
        if (player.wizardHelmet.getEnchantmentLevel(magicQueue) > 0 &&
            sequence?.channelingMagicCount() == 2
        ) {
            channelSpeed += 0.3
        }

        // Queue Acceleration: +60% channel speed for magics third or later in the queue.
        if (player.wizardHelmet.getEnchantmentLevel(queueAcceleration) > 0 &&
            (sequence?.channelingMagicCount()?.toLong() ?: 0L) >= 3
        ) {
            channelSpeed += 0.6
        }

        // Wrap Dancer: +100% channel speed.
        if (player.wizardHelmet.item is WizardHelmetWarpDancer) {
            channelSpeed += 1.0
        }

        // Peak Overdrive: + 50% channel speed when blood pact is activated.
        if (player.wizardHelmet.getEnchantmentLevel(peakOverdrive) > 0 &&
            player.bloodPactActive
        ) {
            channelSpeed += 0.5
        }

        basicChannelTime /= channelSpeed
        return floor(basicChannelTime).toLong().coerceAtLeast(0)
    }

    open fun getBloodPactConvertRatio(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Double {
        var ratio = 2.0

        // Peak Overdrive: + 100% health to mana conversion efficiency.
        if (player.wizardHelmet.getEnchantmentLevel(peakOverdrive) > 0 &&
            player.bloodPactActive
        ) {
            ratio += 1.0
        }

        return ratio
    }

    val id: Int
        get() = name.hashCode()
}

fun LivingEntity.isInvulnerableToEffect(effect: RegistryEntry<StatusEffect>): Boolean {
    if (!canHaveStatusEffect(StatusEffectInstance(effect, 0, 0))) {
        return true
    }

    if (this is EnderDragonEntity || this is WitherEntity) {
        return true
    }

    return false
}