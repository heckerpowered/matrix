/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import heckerpowered.matrix.client.TimeController
import heckerpowered.matrix.client.render.particle.module.particle_spawn.RandomLifetimeModule
import heckerpowered.matrix.client.render.particle.module.particle_update.DragModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ScaleSpriteSizeBySpeedModule
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.renderer.rendertype.MatrixRenderTypes
import net.minecraft.util.ARGB
import net.minecraft.world.phys.Vec3
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.DurationUnit

object MatrixPointSpriteParticles {
    private const val PARTICLE_CAPACITY = 10000
    private const val SPRITE_SIZE = 80.0F
    private const val COLOR_INTENSITY = 4.0F

    private val trailSystem = CpuPointSpriteSystem(
        capacity = PARTICLE_CAPACITY,
        baseColor = color(1.0F, 0.5F, 1.0F, 1.0F),
        colorIntensity = COLOR_INTENSITY,
        speedRange = Vector2f(.0F, 1.0F),
        velocityMultiplier = Vector3f(1.0F, 1.0F, 1.0F),
    )
    private val explosionSystem = CpuPointSpriteSystem(
        capacity = PARTICLE_CAPACITY,
        baseColor = color(0.1F, 0.5F, 1.0F, 1.0F),
        colorIntensity = COLOR_INTENSITY,
        speedRange = Vector2f(.0F, 20.0F),
        velocityMultiplier = Vector3f(10.0F, 10.0F, 10.0F),
    )
    private var initialized = false

    @JvmStatic
    fun onInitialize() {
        if (initialized) {
            return
        }
        initialized = true
        LevelRenderEvents.COLLECT_SUBMITS.register(::collectSubmits)
    }

    fun spawnTrailParticles(position: Vec3, count: Int) {
        trailSystem.spawnPartial(position, count)
    }

    fun spawnExplosionParticles(position: Vec3, speedRange: Vector2f, velocityMultiplier: Vector3f) {
        explosionSystem.speedRange.set(speedRange)
        explosionSystem.velocityMultiplier.set(velocityMultiplier)
        explosionSystem.spawnAll(position)
    }

    private fun collectSubmits(context: LevelRenderContext) {
        updateParticles()
        if (!trailSystem.hasActiveParticles && !explosionSystem.hasActiveParticles) {
            return
        }

        val cameraPosition = context.levelState().cameraRenderState.pos
        val poseStack = PoseStack()
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z)

        context.submitNodeCollector().submitCustomGeometry(
            poseStack,
            MatrixRenderTypes.matrixPointSprite(MatrixShaderPipelines.worldPointSpritePipeline()),
        ) { pose, vertexConsumer ->
            trailSystem.writeVertices(pose, vertexConsumer)
            explosionSystem.writeVertices(pose, vertexConsumer)
        }
    }

    private fun updateParticles() {
        val deltaSeconds = TimeController.strictDeltaTime.toDouble(DurationUnit.SECONDS).toFloat()
        if (deltaSeconds <= .0F) {
            return
        }
        trailSystem.update(deltaSeconds)
        explosionSystem.update(deltaSeconds)
    }

    private class CpuPointSpriteSystem(
        private val capacity: Int,
        private val baseColor: Int,
        private val colorIntensity: Float,
        val speedRange: Vector2f,
        val velocityMultiplier: Vector3f,
    ) {
        private val particles = Array(capacity) { Particle() }
        private var spawnCursor = 0
        private var activeParticleCount = 0

        val hasActiveParticles: Boolean
            get() = activeParticleCount > 0

        fun spawnPartial(position: Vec3, count: Int) {
            val particleCount = count.coerceAtLeast(0)
            if (particleCount == 0) {
                return
            }

            spawnCursor += particleCount
            if (spawnCursor > capacity) {
                spawnCursor = 0
            }
            spawnRange(position, spawnCursor, particleCount)
        }

        fun spawnAll(position: Vec3) {
            clear()
            spawnCursor = 0
            spawnRange(position, 0, capacity)
        }

        fun update(deltaSeconds: Float) {
            if (activeParticleCount == 0) {
                return
            }

            for (index in particles.indices) {
                val particle = particles[index]
                if (!particle.active) {
                    continue
                }
                if (particle.age > particle.lifetime) {
                    particle.active = false
                    activeParticleCount--
                    continue
                }

                val newVelocityX = particle.velocityX + particle.accelerationX * deltaSeconds
                val newVelocityY = particle.velocityY + particle.accelerationY * deltaSeconds
                val newVelocityZ = particle.velocityZ + particle.accelerationZ * deltaSeconds
                particle.x += newVelocityX * deltaSeconds
                particle.y += newVelocityY * deltaSeconds
                particle.z += newVelocityZ * deltaSeconds
                particle.velocityX = newVelocityX
                particle.velocityY = newVelocityY
                particle.velocityZ = newVelocityZ
                particle.age += deltaSeconds

                val drag = randomRangeFloat(index, DragModule.minDrag, DragModule.maxDrag)
                val dragFactor = max(.0F, 1.0F - drag * deltaSeconds)
                particle.velocityX *= dragFactor
                particle.velocityY *= dragFactor
                particle.velocityZ *= dragFactor

                val speed = sqrt(
                    particle.velocityX * particle.velocityX +
                        particle.velocityY * particle.velocityY +
                        particle.velocityZ * particle.velocityZ,
                )
                val normalizedSpeed = (speed / ScaleSpriteSizeBySpeedModule.velocityThreshold).coerceIn(.0F, 1.0F)
                particle.scale = lerp(
                    ScaleSpriteSizeBySpeedModule.minScaleFactor,
                    ScaleSpriteSizeBySpeedModule.maxScaleFactor,
                    normalizedSpeed,
                )
            }
        }

        fun writeVertices(pose: PoseStack.Pose, vertexConsumer: VertexConsumer) {
            if (activeParticleCount == 0) {
                return
            }

            for (particle in particles) {
                if (!particle.active) {
                    continue
                }
                vertexConsumer.addVertex(pose, particle.x, particle.y, particle.z)
                    .setUv(particle.spriteSize * particle.scale, colorIntensity)
                    .setColor(baseColor)
            }
        }

        private fun clear() {
            for (particle in particles) {
                particle.active = false
            }
            activeParticleCount = 0
        }

        private fun spawnRange(position: Vec3, first: Int, count: Int) {
            val start = first.coerceIn(0, capacity)
            val actualCount = count.coerceIn(0, capacity - start)
            if (actualCount <= 0) {
                return
            }

            val time = ((System.currentTimeMillis() % 10000L) / 1000.0).toFloat()
            for (offset in 0 until actualCount) {
                val index = start + offset
                val particle = particles[index]
                if (!particle.active) {
                    activeParticleCount++
                }
                initializeParticle(particle, index, position, time)
            }
        }

        private fun initializeParticle(particle: Particle, index: Int, position: Vec3, time: Float) {
            val velocityDirection = randomDirection(index + time)
            val randomSpeed = randomScalarInRange(index + 114.514F, speedRange.x, speedRange.y)

            particle.active = true
            particle.x = position.x.toFloat()
            particle.y = position.y.toFloat()
            particle.z = position.z.toFloat()
            particle.velocityX = velocityDirection.x * randomSpeed * velocityMultiplier.x
            particle.velocityY = velocityDirection.y * randomSpeed * velocityMultiplier.y
            particle.velocityZ = velocityDirection.z * randomSpeed * velocityMultiplier.z
            particle.accelerationX = .0F
            particle.accelerationY = .0F
            particle.accelerationZ = .0F
            particle.spriteSize = SPRITE_SIZE
            particle.scale = 1.0F
            particle.age = .0F
            particle.lifetime = randomRangeFloat(index, RandomLifetimeModule.minLifetime, RandomLifetimeModule.maxLifetime)
        }
    }

    private class Particle {
        var active = false
        var x = .0F
        var y = .0F
        var z = .0F
        var velocityX = .0F
        var velocityY = .0F
        var velocityZ = .0F
        var accelerationX = .0F
        var accelerationY = .0F
        var accelerationZ = .0F
        var spriteSize = SPRITE_SIZE
        var scale = 1.0F
        var age = .0F
        var lifetime = .0F
    }

    private fun randomDirection(seed: Float): Vector3f {
        val u = hash(seed)
        val v = hash(seed + 1.0F)
        val theta = u * 2.0F * PI.toFloat()
        val phi = acos((2.0F * v - 1.0F).coerceIn(-1.0F, 1.0F))
        val sinPhi = sin(phi)
        return Vector3f(
            sinPhi * cos(theta),
            sinPhi * sin(theta),
            cos(phi),
        )
    }

    private fun randomScalarInRange(seed: Float, minValue: Float, maxValue: Float): Float {
        return lerp(minValue, maxValue, hash(seed))
    }

    private fun hash(value: Float): Float {
        return fract(sin(value) * 43758.5453F)
    }

    private fun randomRangeFloat(vertexId: Int, minValue: Float, maxValue: Float): Float {
        var seed = vertexId
        seed = (seed xor 61) xor (seed ushr 16)
        seed *= 9
        seed = seed xor (seed ushr 4)
        seed *= 0x27d4eb2d
        seed = seed xor (seed ushr 15)
        val normalized = (seed and 0x00FFFFFF).toFloat() / 0x01000000.toFloat()
        return lerp(minValue, maxValue, normalized)
    }

    private fun lerp(from: Float, to: Float, amount: Float): Float {
        return from + (to - from) * amount
    }

    private fun fract(value: Float): Float {
        return value - floor(value)
    }

    private fun color(red: Float, green: Float, blue: Float, alpha: Float): Int {
        return ARGB.color(
            (alpha.coerceIn(.0F, 1.0F) * 255.0F).toInt(),
            (red.coerceIn(.0F, 1.0F) * 255.0F).toInt(),
            (green.coerceIn(.0F, 1.0F) * 255.0F).toInt(),
            (blue.coerceIn(.0F, 1.0F) * 255.0F).toInt(),
        )
    }
}
