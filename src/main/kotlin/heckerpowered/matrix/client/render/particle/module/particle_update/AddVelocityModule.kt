package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.glUniform3f

class AddVelocityModule(var velocity: Vector3f) : ParticleUpdateModule() {
    companion object {
        var velocity = Vector3f()
        val VELOCITY_PROVIDER = UniformProvider("Velocity") { pointer ->
            glUniform3f(pointer, velocity.x, velocity.y, velocity.z)
        }
        private val SHADER = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_update/add_velocity.vsh"),
            uniforms = arrayOf(
                DELTA_TIME_PROVIDER,
                VELOCITY_PROVIDER
            ),
            components = arrayOf(PARTICLE_TRANSFORM_FEEDBACK)
        )
    }

    override fun bind(particleStates: GpuParticleState) {
        AddVelocityModule.velocity = this.velocity
        SHADER.enableShader()
        super.bind(particleStates)
    }

    override fun unbind(particleStates: GpuParticleState) {
        super.unbind(particleStates)
        SHADER.disableShader()
        AddVelocityModule.velocity = Vector3f()
    }
}