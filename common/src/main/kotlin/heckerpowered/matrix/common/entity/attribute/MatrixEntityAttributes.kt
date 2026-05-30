/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.attribute

import heckerpowered.matrix.Matrix
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute


object MatrixEntityAttributes {
    @JvmField
    val magicResistance = register(
        "magic_resistance",
        UnboundedAttribute("attribute.name.matrix_magic_resistance", .0)
            .setSyncable(true)
    )

    fun onInitialize() {
    }

    private fun register(@Suppress("SameParameterValue") name: String, attribute: Attribute): Holder<Attribute> {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Matrix.identifier(name), attribute)
    }

    val LivingEntity.magicResistance: Double
        get() = getAttribute(MatrixEntityAttributes.magicResistance)?.value ?: .0
}