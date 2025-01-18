package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.network.ServerPlayerEntity

class MemoryEraseMagic : Magic(
    MatrixLanguage.magicMemoryErase,
    10,
    MatrixLanguage.magicMemoryEraseDescription,
    10
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.brain.forgetAll()
        if (target is MobEntity) {
            target.target = null
        }
        if (target is Angerable) {
            target.stopAnger()
        }
        target.attacker = null
    }
}