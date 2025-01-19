package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.Angerable
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.village.VillageGossipType

class MemoryEraseMagic : Magic(
    MatrixLanguage.magicMemoryErase,
    10,
    MatrixLanguage.magicMemoryEraseDescription,
    10
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence) {
        target.brain.clear()
        if (target is MobEntity) {
            target.target = null
            target.targetSelector.goals
                .map { it.goal }
                .forEach { it.stop() }
        }
        if (target is Angerable) {
            target.stopAnger()
        }
        target.attacker = null

        if (player == null) {
            return
        }
        if (target is VillagerEntity) {
            val reputation = target.getReputation(player)
            if (reputation < 0) {
                val gossips = target.gossip.entityReputationAssociatedGossips[player.uuid]
                gossips?.set(VillageGossipType.MAJOR_NEGATIVE, 0)
                gossips?.set(VillageGossipType.MINOR_NEGATIVE, 0)
            }
        }
    }
}