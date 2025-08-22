/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_COLOR_LOGIC_OP

class ColorLogicOpState(enabled: Boolean) : CapabilityState(GL_COLOR_LOGIC_OP, enabled)