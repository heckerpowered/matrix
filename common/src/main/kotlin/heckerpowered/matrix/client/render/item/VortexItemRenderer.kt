/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.item

// TODO(26.2): VortexItemRenderer had no live body even before this port (its `render()` was
// entirely commented out — see git history), so there is nothing to carry over behaviorally.
// It is left unported rather than stubbed with a fake API surface because the 26.2 replacement
// mechanism is structurally different, not just renamed:
//
//   - `net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry` /
//     `DynamicItemRenderer` (imperative "register a renderer object for this Item" hook) is
//     gone entirely — not present anywhere under `net.fabricmc.fabric.api.client.rendering.v1`
//     in the 26.2 Fabric API.
//   - Custom per-item GPU rendering is now done via
//     `net.minecraft.client.renderer.special.SpecialModelRenderer<T>`, which is wired up
//     *data-driven*: a `SpecialModelRenderer.Unbaked<T>` implementation registered as a
//     `MapCodec` in `SpecialModelRenderers.ID_MAPPER` (populated from
//     `SpecialModelRenderers.bootstrap()`, a vanilla-internal static list — no Fabric API
//     extension point for adding entries was found in the client.rendering.v1 dump), *plus*
//     the item's JSON model (`assets/matrix/models/item/...json`) needs a
//     `"type": "minecraft:special"` model referencing that registered id.
//
// Reintroducing the vortex item visual therefore needs a resources/datagen change (new item
// model JSON + registered Unbaked/codec type) in addition to Kotlin code, which is out of
// scope for this renderer-only port pass. See scratchpad render-port-pattern.md priority 6.
//
// If/when this is revisited: `MagicTalismanItem` (heckerpowered.matrix.common.item) is the
// item this used to decorate; `VortexRenderer.vortexShader` (heckerpowered.matrix.client.render.shader,
// not yet ported either) held the shader that painted the vortex effect into the quad this
// used to draw via Tessellator/BufferRenderer (also gone, see LegacyMatrixUIRenderer.kt notes).
// The MatrixClient.kt registration line (`BuiltinItemRendererRegistry.INSTANCE.register(...)`)
// has been removed to match.
