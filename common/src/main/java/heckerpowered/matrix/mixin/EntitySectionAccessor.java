/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntitySection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** See {@link TickRateManagerAccessor} for why this is an accessor, not a tweaker widening. */
@Mixin(EntitySection.class)
public interface EntitySectionAccessor {
    @Accessor("storage")
    ClassInstanceMultiMap<?> matrix$getStorage();
}
