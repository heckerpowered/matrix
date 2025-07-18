package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_DEPTH_TEST

class DepthTestState(enabled: Boolean) : CapabilityState(GL_DEPTH_TEST, enabled)