package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object DecisiveStrikeMagic : Magic(MatrixLanguage.magicDecisiveStrike, 16, MatrixLanguage.magicDecisiveStrikeDescription, 20 * 3) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { target.world.damageSources.create(MatrixDamageTypes.magic, player) }
        target.timeUntilRegen = 0

        // Each consumed mana increases the damage by 1%, up to a maximum of 400%
        val castedMagicCost = (sequence.castedMagics().sumOf { it.cost }).coerceAtLeast(0)

        val baseDamage = 6.0
        val damageIncreaseBasedOnMaxHealth = (target.maxHealth * 0.14).coerceAtLeast(.0)
        val playerAttackDamage = player?.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) ?: .0

        val damageMultiplierBasedOnCost = (castedMagicCost * 0.01).coerceIn(.0..4.0)
        val damageMultiplierBloodPact = if (player?.bloodPactActive == true) {
            1.0
        } else {
            .0
        }

        val damage = baseDamage + damageIncreaseBasedOnMaxHealth + playerAttackDamage
        val amount = damage * (1 + damageMultiplierBasedOnCost + damageMultiplierBloodPact)

        target.damage(damageSource, amount.toFloat())
        player?.addCritParticles(target)
        player?.addEnchantedHitParticles(target)
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        val damageSource = if (sequence?.sequencedAfter<MemoryEraseMagic>() == true) {
            player.world.damageSources.create(MatrixDamageTypes.magic)
        } else {
            player.world.damageSources.create(MatrixDamageTypes.magic, player)
        }

        if (target?.isInvulnerable == true || target?.isInvulnerableTo(damageSource) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }

    override fun getChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence?, data: MagicData): Long {
        val index = sequence?.index ?: 0
        val channelTime = super.getChannelTime(player, target, sequence, data)

        val reducedChannelTime = (channelTime * (index * 0.2)).toLong()
        return (channelTime - reducedChannelTime).coerceAtLeast(1)
    }
}