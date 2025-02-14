package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EntityType
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object LightningBoltMagic : Magic(MatrixLanguage.magicLightningBoltMagic, 20, MatrixLanguage.magicLightningBoltMagicDescription, 20) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.world.spawnEntity(LightningEntity(EntityType.LIGHTNING_BOLT, target.world).also {
            it.setPosition(target.pos)
        })
    }
}