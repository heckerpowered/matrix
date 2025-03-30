package heckerpowered.matrix.common.item

import com.mojang.serialization.Codec
import heckerpowered.matrix.Matrix
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val redstoneSuitPowerComponent
    get() = MatrixComponents.redstoneSuitPower

val redstoneSuitMaxPowerComponent
    get() = MatrixComponents.redstoneSuitMaxPower

val wizardHelmetMaxManaComponent
    get() = MatrixComponents.maxMana

val borrowedTimeChargeComponent
    get() = MatrixComponents.borrowedTimeCharge

val borrowedTimeMaxChargeComponent
    get() = MatrixComponents.borrowedTimeMaxCharge

val borrowedTimeStateComponent
    get() = MatrixComponents.borrowedTimeState

object MatrixComponents {
    val redstoneSuitPower = register("redstone_suit_power") { codec(Codec.LONG) }
    val redstoneSuitMaxPower = register("redstone_suit_max_power") { codec(Codec.LONG) }

    val maxMana = register("wizard_helmet_max_mana") { codec(Codec.DOUBLE) }

    val load = register("load") { codec(Codec.DOUBLE) }
    val maxLoad = register("max_load") { codec(Codec.DOUBLE) }

    val borrowedTimeCharge = register("borrowed_time_charge") { codec(Codec.LONG) }
    val borrowedTimeMaxCharge = register("borrowed_time_max_charge") { codec(Codec.LONG) }
    val borrowedTimeState = register("borrowed_time_state") { codec(Codec.BOOL) }

    @OptIn(ExperimentalContracts::class)
    private fun <T> register(name: String, register: ComponentType.Builder<T>.() -> Unit): ComponentType<T> {
        contract {
            callsInPlace(register, InvocationKind.EXACTLY_ONCE)
        }

        val identifier = Matrix.identifier(name)
        val builder = ComponentType.Builder<T>()
        register(builder)
        return Registry.register(Registries.DATA_COMPONENT_TYPE, identifier, builder.build())
    }

    fun onInitialize() {}
}