package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.MatrixStatusEffects
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.text.Text

class ManaOverloadMagic : Magic(
    MatrixLanguage.magicManaOverload,
    10
) {
    override fun onUse(player: PlayerEntity, target: Entity) {
        if (target !is LivingEntity) {
            return
        }

        target.addStatusEffect(
            StatusEffectInstance(
                Registries.STATUS_EFFECT.getEntry(MatrixStatusEffects.manaOverload),
                20 * 5,
                0,
                true,
                false
            )
        )
    }

    override fun getDescription(): List<Text> {
        return listOf(
            MatrixLanguage.magicManaOverloadDescription1,
            MatrixLanguage.magicManaOverloadDescription2,
            MatrixLanguage.magicManaOverloadDescription3,
            MatrixLanguage.magicManaOverloadDescription4,
            MatrixLanguage.magicManaOverloadDescription5
        )
    }
}