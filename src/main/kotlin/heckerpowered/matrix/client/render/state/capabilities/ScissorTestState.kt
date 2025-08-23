/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_SCISSOR_TEST

class ScissorTestState(enabled: Boolean) : CapabilityState(GL_SCISSOR_TEST, enabled)