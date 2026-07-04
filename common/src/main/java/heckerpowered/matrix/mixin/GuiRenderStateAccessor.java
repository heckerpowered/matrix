/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRenderState.class)
public interface GuiRenderStateAccessor {
    @Accessor("firstStratumAfterBlur")
    int matrix$getFirstStratumAfterBlur();

    @Accessor("firstStratumAfterBlur")
    void matrix$setFirstStratumAfterBlur(int value);

    @Accessor("strata")
    java.util.List<?> matrix$getStrata();
}
