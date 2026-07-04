/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.extension

/**
 * Injected into GuiRenderState (classtweaker inject-interface + soft @Implements in
 * GuiRenderStateMixin). Marks where the Matrix HUD's stratum begins so GuiRendererMixin can
 * split the vanilla draw list and capture the mod's HUD into its own framebuffer — the 26.2
 * replacement for 1.21's "bind hudFramebuffer while the HUD draws" capture semantics.
 */
interface MatrixGuiRenderState {
    /** Starts a fresh stratum and remembers it as the beginning of the Matrix HUD content. */
    fun beginMatrixHudStratum()

    /**
     * Starts a fresh stratum and remembers it as the END boundary of the Matrix HUD content:
     * everything the HUD callback extracted lives in [start, end), and later strata
     * (screens, tooltips, toasts) stay out of the capture.
     */
    fun endMatrixHudStratum()

    /**
     * Whether a Matrix HUD stratum was begun this frame AND the capture split is applicable
     * (no vanilla menu-blur split active; with one the renderer degrades to vanilla behavior).
     */
    fun hasMatrixHudStratum(): Boolean

    /** Index into the strata list where the Matrix HUD content begins, or -1. */
    fun matrixHudStrataStart(): Int

    /** Exclusive end index of the Matrix HUD strata, or Integer.MAX_VALUE if never marked. */
    fun matrixHudStrataEnd(): Int
}
