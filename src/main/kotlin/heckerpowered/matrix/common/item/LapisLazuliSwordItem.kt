package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.magics.DecisiveStrikeMagic
import heckerpowered.matrix.common.network.ChannelMagicPayload
import heckerpowered.matrix.common.persistent.ChannelSequence
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.server.network.ServerPlayerEntity

object LapisLazuliSwordItem : SwordItem(
    lapisLazuliToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(lapisLazuliToolMaterial, 3, -2.4F))
) {
    override fun postDamageEntity(stack: ItemStack, target: LivingEntity, attacker: LivingEntity) {
        super.postDamageEntity(stack, target, attacker)
        if ((0..100).random() !in 0..10) {
            return
        }

        if (attacker is ServerPlayerEntity) {
            if (ChannelSequence.channelMagic(DecisiveStrikeMagic, attacker, target, false)) {
                ServerPlayNetworking.send(attacker, ChannelMagicPayload(DecisiveStrikeMagic.id, target.id))
            }
        } else {
            DecisiveStrikeMagic.cast(null, target, ChannelSequence(target))
        }
    }
}