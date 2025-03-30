package heckerpowered.matrix.core

import heckerpowered.matrix.Matrix
import net.minecraft.entity.player.PlayerEntity

val PlayerEntity.mana: Double
    get() = Matrix.proxy.getPlayerMana(this)

val PlayerEntity.maxMana: Double
    get() = Matrix.proxy.getPlayerMaxMana(this)