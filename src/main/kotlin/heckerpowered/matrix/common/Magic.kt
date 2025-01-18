package heckerpowered.matrix.common

import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.common.magics.MagicAvailableStatus
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.mana
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

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
    private val channelTime: Long = 10
) {
    /**
     * Called when the magic is casting.
     */
    open fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {}
    open fun channel(player: ServerPlayerEntity, target: LivingEntity, sequence: ChannelSequence) {}

    open fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): MagicAvailableStatus {
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

    protected open fun checkChannelSequenceIsLocked(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): Boolean {
        return sequence?.locked ?: false
    }

    /**
     * Check whether the channel sequence is full.
     */
    protected open fun checkChannelSequenceIsFull(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): Boolean {
        if (sequence == null) {
            return false
        }

        if (sequence.channelingMagicCount() >= 4) {
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
        val mana = if (player.world.isClient) {
            MatrixHud.mana - MatrixHud.manaUsage
        } else {
            (player as ServerPlayerEntity).mana
        }

        if (getCost(player, target, sequence) > mana) {
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
        return cost
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
        return channelTime
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