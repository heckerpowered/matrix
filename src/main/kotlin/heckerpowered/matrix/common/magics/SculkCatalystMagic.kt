package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.sculkCatalystEffect
import heckerpowered.matrix.common.network.ChannelMagicPayload
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity

object SculkCatalystMagic : Magic(MatrixLanguage.sculkCatalystMagic, 30, MatrixLanguage.sculkCatalystMagicDescription, 100) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        val effect = target.getStatusEffect(sculkCatalystEffect)
        val amplifier = (effect?.amplifier ?: 0) + 1
        val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.create(MatrixDamageTypes.magic, player) }
        target.damage(damageSource, 20.0F + (amplifier - 1) * 10.0F)
        if (target.isAlive || player == null) {
            return
        }

        target.removeStatusEffect(sculkCatalystEffect)
        val targets = target.world.getOtherEntities(player, target.boundingBox.expand(20.0, 20.0, 20.0)).sortedBy {
            it.squaredDistanceTo(target)
        }
        for (entity in targets) {
            if (entity !is LivingEntity || entity == target || !entity.isAlive) {
                continue
            }

            val effectInstance = StatusEffectInstance(sculkCatalystEffect, 200, amplifier + 1)
            entity.addStatusEffect(effectInstance)
            player.networkHandler.sendPacket(EntityStatusEffectS2CPacket(entity.id, effectInstance, false))
            if (ChannelSequence.channelMagic(SculkCatalystMagic, player, entity, true)) {
                ServerPlayNetworking.send(player, ChannelMagicPayload(id, entity.id))
                break
            } else {
                entity.removeStatusEffect(sculkCatalystEffect)
            }
        }
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): Long {
        val effect = target?.getStatusEffect(sculkCatalystEffect) ?: return super.getCost(player, target, sequence)
        (effect.amplifier + 1).coerceAtMost(5)

        return super.getCost(player, target, sequence)
    }

    override fun getChannelTime(player: PlayerEntity, target: LivingEntity, sequence: ChannelSequence?): Long {
        val effect = target.getStatusEffect(sculkCatalystEffect)
        val amplifier = ((effect?.amplifier ?: 0) + 1)

        return (super.getChannelTime(player, target, sequence) - (amplifier * 10)).coerceAtLeast(10)
    }
}