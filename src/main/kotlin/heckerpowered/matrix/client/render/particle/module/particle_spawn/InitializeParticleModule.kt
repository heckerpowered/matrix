package heckerpowered.matrix.client.render.particle.module.particle_spawn

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.particle.ParticleState
import heckerpowered.matrix.client.render.particle.memory.MemoryLayout
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformBufferProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL46
import org.lwjgl.system.MemoryUtil
import java.lang.AutoCloseable

class InitializeParticleModule() : ParticleSpawnModule(), AutoCloseable {
    companion object {
        private var particleState: ParticleState? = null
        private val shader = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_spawn/initialize_particle.vsh"),
            // uniforms = arrayOf(
            //     UniformProvider("Position") { pointer -> GL20.glUniform3fv(pointer, particleState!!.positionBuffer) },
            //     UniformProvider("Velocity") { pointer -> GL20.glUniform3fv(pointer, particleState!!.velocityBuffer) },
            //     UniformProvider("Acceleration") { pointer -> GL20.glUniform3fv(pointer, particleState!!.accelerationBuffer) },
            //     UniformProvider("Color") { pointer -> GL20.glUniform4fv(pointer, particleState!!.colorBuffer) },
            //     UniformProvider("Orientation") { pointer -> GL20.glUniform4fv(pointer, particleState!!.orientationBuffer) },
            //     UniformProvider("AngularVelocity") { pointer -> GL20.glUniform3fv(pointer, particleState!!.angularVelocityBuffer) },
            //     UniformProvider("Age") { pointer -> GL20.glUniform1f(pointer, particleState!!.age) },
            //     UniformProvider("Lifetime") { pointer -> GL20.glUniform1f(pointer, particleState!!.lifetime) },
            //     UniformProvider("SpriteSize") { pointer -> GL20.glUniform1f(pointer, particleState!!.spriteSize) },
            //     UniformProvider("Scale") { pointer -> GL20.glUniform1f(pointer, particleState!!.scale) }
            // ),
            uniformBuffers = arrayOf(
                UniformBufferProvider("ParticleState") { program, pointer ->
                    val particleState = particleState ?: return@UniformBufferProvider
                    val uniformBuffer = particleState.layout.sharedUniformBufferObject
                    GL46.glUniformBlockBinding(program, pointer, 0)

                    GL46.glBindBufferBase(GL46.GL_UNIFORM_BUFFER, 0, uniformBuffer)
                    GL46.glBufferSubData(GL46.GL_UNIFORM_BUFFER, 0, particleState.data)
                }
            ),
            components = arrayOf(TRANSFORM_FEEDBACK)
        )
    }

    val particleState = ParticleState(MemoryUtil.memCallocFloat(28), MemoryLayout.STD_140)

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        InitializeParticleModule.particleState = particleState
        shader.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        shader.disableShader()
        InitializeParticleModule.particleState = null
    }

    override fun close() {
        MemoryUtil.memFree(particleState.data)
    }
}
