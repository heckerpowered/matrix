package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.core.approximatelyEqual
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import kotlin.math.floor

class ScalingFramebuffer(width: Int, height: Int, useDepth: Boolean, getError: Boolean = MinecraftClient.IS_SYSTEM_MAC, private val resolutionScaling: Double) : SimpleFramebuffer(width, height, useDepth, getError) {
    val actualWidth
        get() = floor(textureWidth * resolutionScaling).toInt()

    val actualHeight
        get() = floor(textureHeight * resolutionScaling).toInt()

    init {
        resize(width, height, getError)
    }

    override fun resize(width: Int, height: Int, getError: Boolean) {
        if (resolutionScaling.approximatelyEqual(.0)) {
            super.resize(width, height, getError)
            return
        }

        val scaledWidth = floor(width * resolutionScaling).toInt()
        val scaledHeight = floor(height * resolutionScaling).toInt()
        super.resize(scaledWidth, scaledHeight, getError)
    }
}