/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** See {@link TickRateManagerAccessor} for why this is an accessor, not a tweaker widening. */
@Mixin(Brain.class)
public interface BrainAccessor {
    @Accessor("memories")
    Map<MemoryModuleType<?>, MemorySlot<?>> matrix$getMemories();
}
