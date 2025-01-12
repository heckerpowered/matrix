package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

class SculkCatalystMagic : Magic(
    MatrixLanguage.sculkCatalystMagic,
    20,
    MatrixLanguage.sculkCatalystMagicDescription,
    20
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {

    }
}