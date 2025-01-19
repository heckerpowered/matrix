package heckerpowered.matrix.common.effect

import heckerpowered.matrix.Matrix
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

val manaOverloadEffect by lazy { Registries.STATUS_EFFECT.getEntry(ManaOverloadEffect) }
val armorPenetrationEffect by lazy { Registries.STATUS_EFFECT.getEntry(ArmorPenetrationEffect) }
val crippleMovementEffect by lazy { Registries.STATUS_EFFECT.getEntry(CrippleMovementEffect) }
val witherArmorChargedEffect by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorChargedEffect) }
val witherArmorEffect by lazy { Registries.STATUS_EFFECT.getEntry(WitherArmorEffect) }
val angeredEffect by lazy { Registries.STATUS_EFFECT.getEntry(AngeredEffect) }

object MatrixStatusEffects {
    fun onInitialize() {
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("mana_overload"), ManaOverloadEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("armor_penetration"), ArmorPenetrationEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("cripple_movement"), CrippleMovementEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("wither_armor_charged"), WitherArmorChargedEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("wither_armor"), WitherArmorEffect)
        Registry.register(Registries.STATUS_EFFECT, Matrix.identifier("angered"), AngeredEffect)
    }
}