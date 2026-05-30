package heckerpowered.matrix.extension

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.entity.LevelEntityGetter

interface MatrixLevel {
    val entityGetter: LevelEntityGetter<Entity>
}