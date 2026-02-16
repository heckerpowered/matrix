/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.resource

@JvmInline
value class Mana(private val amount: Double) {
    companion object {
        val Number.mana: Mana
            get() = Mana(toDouble())

        operator fun Mana.plus(other: Mana): Mana = Mana(this.amount + other.amount)
        operator fun Mana.minus(other: Mana): Mana = Mana(this.amount - other.amount)
        operator fun Mana.times(factor: Double): Mana = Mana(this.amount * factor)
        operator fun Mana.div(divisor: Double): Mana = Mana(this.amount / divisor)
        operator fun Mana.div(other: Mana): Double = amount / other.amount
        operator fun Mana.compareTo(other: Mana): Int = this.amount.compareTo(other.amount)
    }

    fun toDouble(): Double = amount
}