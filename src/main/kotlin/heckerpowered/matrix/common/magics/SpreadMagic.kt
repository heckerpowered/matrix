package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object SpreadMagic : Magic(MatrixLanguage.magicSpread, 9, MatrixLanguage.magicSpreadDescription, 9) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        if (player == null) {
            return
        }
        val magics = sequence.magics.filterIndexed { index, channelingMagic ->
            index > sequence.index && channelingMagic.magic != this
        }.map {
            it.magic
        }
        target.world.getOtherEntities(player, target.boundingBox.expand(24.0)).forEach {
            if (it !is LivingEntity || it == target) {
                return@forEach
            }
            for (magic in magics) {
                ChannelSequence.channelMagic(magic, player, it)
            }
        }
    }
}