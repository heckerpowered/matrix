package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
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

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelSequence?
    ): MagicAvailableStatus {
        var isValid = false
        if (target is MobEntity && target.target != null) {
            isValid = true
        }
        if (target is Angerable && target.angryAt != null) {
            isValid = true
        }
        if (target?.attacker != null) {
            isValid = true
        }
        if (!isValid) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }
}