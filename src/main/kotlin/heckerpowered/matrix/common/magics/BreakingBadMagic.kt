package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.isInvulnerableToEffect
import heckerpowered.matrix.common.magics.ExplosionMagic.explosionBehavior
import heckerpowered.matrix.common.network.ChannelMagicPayload
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.getChannelSequence
import heckerpowered.matrix.core.toBox
import heckerpowered.matrix.data.language.MatrixLanguage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

object BreakingBadMagic : Magic(MatrixLanguage.magicBreakingBad, 10, MatrixLanguage.magicBreakingBadDescription, 40) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        target.addStatusEffect(StatusEffectInstance(StatusEffects.POISON, 20 * 5, 4))
        target.addStatusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 5, 4))

        if (target.isOnFire) {
            val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.explosion(target, player) }
            target.world.createExplosion(player, damageSource, explosionBehavior, target.x, target.y, target.z, 4.0F, false, World.ExplosionSourceType.MOB)
        }

        if (player == null || data.isSpread) {
            return
        }

        fun getNearestEntities(entity: Entity): Entity? {
            val list = entity.world.getOtherEntities(player, entity.pos.toBox().expand(8.0)) {
                it is LivingEntity && (it.getChannelSequence(player)?.channelingMagicCount() ?: 0) == 0
            }.also {
                it.sortBy { it -> it.squaredDistanceTo(entity) }
            }

            return list.firstOrNull()
        }

        var spreadTarget = target
        repeat(4) {
            val nearestEntity = getNearestEntities(spreadTarget)
            if (nearestEntity == null || nearestEntity !is LivingEntity) {
                return
            }

            if (ChannelSequence.channelMagic(BreakingBadMagic, player, nearestEntity, false, data = MagicData(true))) {
                ServerPlayNetworking.send(player, ChannelMagicPayload(id, nearestEntity.id))
            }

            spreadTarget = nearestEntity
        }
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(StatusEffects.POISON) == true ||
            target?.isInvulnerableToEffect(StatusEffects.BLINDNESS) == true
        ) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}