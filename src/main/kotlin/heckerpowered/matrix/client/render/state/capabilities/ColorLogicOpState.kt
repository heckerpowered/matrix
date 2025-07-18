package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_COLOR_LOGIC_OP

class ColorLogicOpState(enabled: Boolean) : CapabilityState(GL_COLOR_LOGIC_OP, enabled)