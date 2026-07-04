/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.world.level.entity.EntitySectionStorage;
import net.minecraft.world.level.entity.LevelEntityGetterAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** See {@link TickRateManagerAccessor} for why this is an accessor, not a tweaker widening. */
@Mixin(LevelEntityGetterAdapter.class)
public interface LevelEntityGetterAdapterAccessor {
    @Accessor("sectionStorage")
    EntitySectionStorage<?> matrix$getSectionStorage();
}
