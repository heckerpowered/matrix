package heckerpowered.matrix.client.render.entity

import heckerpowered.matrix.client.render.ScreenEffectRenderer
import heckerpowered.matrix.client.render.ScreenEffectRenderer.particleSystem
import heckerpowered.matrix.client.render.particle.module.particle_spawn.InitializeParticleModule
import heckerpowered.matrix.common.entity.FinderArrowEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
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
        val particleState = (particleSystem.particleSpawnModules.first { it is InitializeParticleModule } as InitializeParticleModule).particleState

        particleState.age = 0F
        ScreenEffectRenderer.spawnParticleAt(persistentProjectileEntity.getLerpedPos(g), 10)

        super.render(persistentProjectileEntity, f, g, matrixStack, vertexConsumerProvider, i)
    }

    companion object {
        val TEXTURE: Identifier = Identifier.ofVanilla("textures/entity/projectiles/spectral_arrow.png")
    }
}
