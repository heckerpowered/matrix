package heckerpowered.matrix.client.render.entity

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.Frustum
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.Entity
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
class EmptyRenderer(context: EntityRendererFactory.Context) : EntityRenderer<Entity>(context) {
    override fun shouldRender(entity: Entity, frustum: Frustum, x: Double, y: Double, z: Double): Boolean {
        return false
    }

    override fun getTexture(entity: Entity): Identifier = PlayerScreenHandler.BLOCK_ATLAS_TEXTURE
}