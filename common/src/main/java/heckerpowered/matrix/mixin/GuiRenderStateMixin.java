/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.mixin;

import heckerpowered.matrix.extension.MatrixGuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Stratum bookkeeping for the Matrix HUD capture (see {@link MatrixGuiRenderState}).
 */
@Mixin(GuiRenderState.class)
@Implements(@Interface(iface = MatrixGuiRenderState.class, prefix = "matrix$"))
abstract class GuiRenderStateMixin {
    @Shadow
    @Final
    private List<?> strata;

    @Shadow
    private int firstStratumAfterBlur;

    @Shadow
    public abstract void nextStratum();

    @Unique
    private int matrix$hudStrataStart = -1;

    @Unique
    private int matrix$hudStrataEnd = Integer.MAX_VALUE;

    public void matrix$beginMatrixHudStratum() {
        nextStratum();
        matrix$hudStrataStart = strata.size() - 1;
    }

    public void matrix$endMatrixHudStratum() {
        // The fresh stratum receives everything extracted after the HUD callback (screens,
        // tooltips, toasts), so its index is the exclusive end of the captured segment.
        nextStratum();
        matrix$hudStrataEnd = strata.size() - 1;
    }

    public boolean matrix$hasMatrixHudStratum() {
        // Vanilla's fence defaults to Integer.MAX_VALUE and is lowered by blur markers; even
        // in-world a trailing vanilla overlay stratum can carry one (observed fence = hudStart
        // + 1). The capture split composes as long as the Matrix stratum sits BELOW the fence
        // (its draws then live inside the first draw range); otherwise degrade to vanilla.
        return matrix$hudStrataStart >= 0 && firstStratumAfterBlur > matrix$hudStrataStart;
    }

    public int matrix$matrixHudStrataStart() {
        return matrix$hudStrataStart;
    }

    public int matrix$matrixHudStrataEnd() {
        return matrix$hudStrataEnd;
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void reset(CallbackInfo ci) {
        matrix$hudStrataStart = -1;
        matrix$hudStrataEnd = Integer.MAX_VALUE;
    }
}
