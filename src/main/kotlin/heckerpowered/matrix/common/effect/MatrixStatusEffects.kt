package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object MatrixStatusEffects {
    @JvmField
    val manaOverload = ManaOverloadEffect()

    fun onInitialize() {
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("mana_overload"), manaOverload)
    }
}