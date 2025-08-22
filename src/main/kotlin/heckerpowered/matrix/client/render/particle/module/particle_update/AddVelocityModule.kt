package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.glUniform3f
import org.lwjgl.opengl.GL46

class AddVelocityModule(var velocity: Vector3f) : ParticleUpdateModule() {
    companion object {
        var velocity = Vector3f()
        val VELOCITY_PROVIDER = UniformProvider("Velocity") { pointer ->
            glUniform3f(pointer, velocity.x, velocity.y, velocity.z)
        }
        private val Program = Program(
            ResourceShader("/assets/matrix/shaders/particle/particle_update/add_velocity.vsh", GL46.GL_VERTEX_SHADER),
            uniforms = arrayOf(
                DELTA_TIME_PROVIDER,
                VELOCITY_PROVIDER
            ),
            components = arrayOf(TRANSFORM_FEEDBACK)
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        AddVelocityModule.velocity = this.velocity
        Program.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        Program.disableShader()
        AddVelocityModule.velocity = Vector3f()
    }
}