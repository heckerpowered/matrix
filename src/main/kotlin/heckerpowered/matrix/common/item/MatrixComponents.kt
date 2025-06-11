package heckerpowered.matrix.common.item

import com.mojang.serialization.Codec
import heckerpowered.matrix.Matrix
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object MatrixComponents {
    val REDSTONE_SUIT_POWER = register("redstone_suit_power") { codec(Codec.LONG) }
    val REDSTONE_SUIT_MAX_POWER = register("redstone_suit_max_power") { codec(Codec.LONG) }

    val MAX_MANA = register("wizard_helmet_max_mana") { codec(Codec.DOUBLE) }

    val LOAD = register("load") { codec(Codec.DOUBLE) }
    val MAX_LOAD = register("max_load") { codec(Codec.DOUBLE) }

    val BORROWED_TIME_CHARGE = register("borrowed_time_charge") { codec(Codec.LONG) }
    val BORROWED_TIME_MAX_CHARGE = register("borrowed_time_max_charge") { codec(Codec.LONG) }
    val BORROWED_TIME_STATE = register("borrowed_time_state") { codec(Codec.BOOL) }
    val SHOOT_PER_MINUTE = register("shoot_per_minute") { codec(Codec.LONG) }

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