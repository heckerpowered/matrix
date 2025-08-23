/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_POLYGON_OFFSET_FILL

class PolygonOffsetFillState(enabled: Boolean) : CapabilityState(GL_POLYGON_OFFSET_FILL, enabled)