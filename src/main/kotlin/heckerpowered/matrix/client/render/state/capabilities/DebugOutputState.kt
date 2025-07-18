package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT

class DebugOutputState(enabled: Boolean) : CapabilityState(GL_DEBUG_OUTPUT, enabled)