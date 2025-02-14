package heckerpowered.matrix.common.event

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

data class DamageAccumulator(
    val attacker: LivingEntity?,
    val target: LivingEntity,
    var damageSource: DamageSource,
    val baseDamage: Double,
    var baseDamageBonus: Double = 0.0,
    var damageMultiplier: Double = 1.0,
    var immune: Boolean = false,
) {
    fun accumulateDamage(): Double {
        if (immune) {
            return .0
        }

        return (baseDamage + baseDamageBonus) * damageMultiplier
    }
}