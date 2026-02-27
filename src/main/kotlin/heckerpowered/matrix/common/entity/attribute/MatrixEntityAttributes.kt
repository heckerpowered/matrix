/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.attribute

import heckerpowered.matrix.Matrix
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry


object MatrixEntityAttributes {
    @JvmField
    val MAGIC_RESISTANCE = register(
        "magic_resistance",
        ClampedEntityAttribute("attribute.matrix.magic_resistance", .0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            .setTracked(true)
    )

    fun onInitialize() {
    }

    private fun register(name: String, attribute: EntityAttribute): RegistryEntry<EntityAttribute> {
        return Registry.registerReference(Registries.ATTRIBUTE, Matrix.identifier(name), attribute)
    }

    val LivingEntity.magicResistance: Double
        get() = getAttributeInstance(MAGIC_RESISTANCE)?.value ?: .0
}