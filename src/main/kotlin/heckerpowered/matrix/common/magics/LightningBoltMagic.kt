package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.entity.MagicLightningEntity
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object LightningBoltMagic : Magic(MatrixLanguage.magicLightningBoltMagic, 20, MatrixLanguage.magicLightningBoltMagicDescription, 20) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        val lightningTypes = MagicLightningEntity.LightningType.entries
        val lightningType = if ((0..1000).random() < 6) {
            MagicLightningEntity.LightningType.BLACK
        } else {
            var lightningType = lightningTypes.random()
            while (lightningType == MagicLightningEntity.LightningType.BLACK) {
                lightningType = lightningTypes.random()
            }
            lightningType
        }

        target.world.spawnEntity(MagicLightningEntity(target.world).also {
            it.setPosition(target.pos)
            it.lightningType = lightningType
            if (!sequence.sequencedAfter<MemoryEraseMagic>()) {
                it.channeler = player
            }
        })
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?, data: MagicData): Long {
        val cost = super.getCost(player, target, sequence, data)
        if (sequence == null) {
            return cost
        }

        val count = sequence.magics.filterIndexed { index, channelingMagic ->
            channelingMagic.magic is LightningBoltMagic
        }.count()
        val discount = 1 - (count * 0.2).coerceAtMost(0.8)
        return (cost.toDouble() * discount).toLong()
    }
}