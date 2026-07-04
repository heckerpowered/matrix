/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.minecraft.world.level.entity.EntitySectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** See {@link TickRateManagerAccessor} for why this is an accessor, not a tweaker widening. */
@Mixin(EntitySectionStorage.class)
public interface EntitySectionStorageAccessor {
    @Accessor("sectionIds")
    LongSortedSet matrix$getSectionIds();
}
