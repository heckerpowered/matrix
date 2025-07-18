package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.client.render.ScreenEffectRenderer
import heckerpowered.matrix.client.render.ScreenEffectRenderer.particleSystem
import heckerpowered.matrix.client.render.particle.module.particle_spawn.InitializeParticleModule
import heckerpowered.matrix.common.entity.FinderArrowEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.ProjectileEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class FinderArrowEntityRenderer(context: EntityRendererFactory.Context) : ProjectileEntityRenderer<FinderArrowEntity>(context) {
    override fun getTexture(spectralArrowEntity: FinderArrowEntity): Identifier {
        return TEXTURE
    }

    override fun render(persistentProjectileEntity: FinderArrowEntity, f: Float, g: Float, matrixStack: MatrixStack?, vertexConsumerProvider: VertexConsumerProvider?, i: Int) {
        super.render(persistentProjectileEntity, f, g, matrixStack, vertexConsumerProvider, i)
        val particleState = (particleSystem.particleSpawnModules.first { it is InitializeParticleModule } as InitializeParticleModule).particleState

        particleState.velocityX = 0F
        particleState.velocityY = 0F
        particleState.velocityZ = 0F
        persistentProjectileEntity.getRotationVec(g)

        ScreenEffectRenderer.spawnParticleAt(persistentProjectileEntity.getLerpedPos(g), 10)
    }

    override fun shouldRender(entity: FinderArrowEntity?, frustum: Frustum?, x: Double, y: Double, z: Double): Boolean {
        return true
    }

    companion object {
        val TEXTURE: Identifier = Identifier.ofVanilla("textures/entity/projectiles/spectral_arrow.png")
    }
}
