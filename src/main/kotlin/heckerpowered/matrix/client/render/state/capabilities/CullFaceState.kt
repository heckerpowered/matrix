/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_CULL_FACE

class CullFaceState(enabled: Boolean) : CapabilityState(GL_CULL_FACE, enabled)