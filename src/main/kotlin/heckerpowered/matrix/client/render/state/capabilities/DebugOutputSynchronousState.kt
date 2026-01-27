/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS

class DebugOutputSynchronousState(enabled: Boolean) : CapabilityState(GL_DEBUG_OUTPUT_SYNCHRONOUS, enabled)