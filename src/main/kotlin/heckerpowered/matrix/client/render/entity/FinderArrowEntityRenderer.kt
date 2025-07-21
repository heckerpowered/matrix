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
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

data class GLState(
    val viewport: IntArray,
    val scissorBox: IntArray,
    val depthTest: Boolean,
    val depthFunc: Int,
    val depthMask: Boolean,
    val blend: Boolean,
    val blendSrcRGB: Int,
    val blendDstRGB: Int,
    val cullFace: Boolean,
    val cullFaceMode: Int,
    val frontFace: Int,
    val colorMask: BooleanArray,
    val stencilTest: Boolean,
    val stencilFunc: IntArray,
    val rasterizerDiscard: Boolean,
    val currentProgram: Int,
    val vao: Int,
    val arrayBuffer: Int,
    val elementArrayBuffer: Int,
    val tfBinding: Int,
    val tfBufferBinding: Int,
    val framebufferBinding: Int,
)

fun captureState(): GLState {
    val vp = IntArray(4); GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp)
    val sb = IntArray(4); GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, sb)
    val cm = BooleanArray(4)
    cm[0] = GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK)
    // （GL_COLOR_WRITEMASK 实际返回四个 bool，你可以用 glGetBooleanv 读取到数组）
    val sf = IntArray(3)
    GL11.glGetIntegerv(GL11.GL_STENCIL_FUNC, sf)              // func, ref, mask
    return GLState(
        viewport = vp,
        scissorBox = sb,
        depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST),
        depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC),
        depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK),
        blend = GL11.glIsEnabled(GL11.GL_BLEND),
        blendSrcRGB = GL11.glGetInteger(GL11.GL_BLEND_SRC),
        blendDstRGB = GL11.glGetInteger(GL11.GL_BLEND_DST),
        cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE),
        cullFaceMode = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE),
        frontFace = GL11.glGetInteger(GL11.GL_FRONT_FACE),
        colorMask = cm,
        stencilTest = GL11.glIsEnabled(GL11.GL_STENCIL_TEST),
        stencilFunc = sf,
        rasterizerDiscard = GL11.glIsEnabled(GL30.GL_RASTERIZER_DISCARD),
        currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),
        vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),
        arrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING),
        elementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING),
        tfBinding = GL30.glGetInteger(GL30.GL_TRANSFORM_FEEDBACK_BUFFER_BINDING),
        tfBufferBinding = GL30.glGetInteger(GL30.GL_TRANSFORM_FEEDBACK_BUFFER_BINDING),
        framebufferBinding = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)
    )
}

fun diffState(before: GLState, after: GLState) {
    // Viewport
    if (!before.viewport.contentEquals(after.viewport)) {
        println("VIEWPORT: ${before.viewport.toList()} -> ${after.viewport.toList()}")
    }
    // Scissor box
    if (!before.scissorBox.contentEquals(after.scissorBox)) {
        println("SCISSOR_BOX: ${before.scissorBox.toList()} -> ${after.scissorBox.toList()}")
    }
    // Color mask (RGBA)
    if (!before.colorMask.contentEquals(after.colorMask)) {
        println("COLOR_WRITEMASK: ${before.colorMask.toList()} -> ${after.colorMask.toList()}")
    }
    // Depth test
    if (before.depthTest != after.depthTest) {
        println("DEPTH_TEST: ${before.depthTest} -> ${after.depthTest}")
    }
    if (before.depthFunc != after.depthFunc) {
        println("DEPTH_FUNC: ${before.depthFunc} -> ${after.depthFunc}")
    }
    if (before.depthMask != after.depthMask) {
        println("DEPTH_WRITEMASK: ${before.depthMask} -> ${after.depthMask}")
    }
    // Blend
    if (before.blend != after.blend) {
        println("BLEND: ${before.blend} -> ${after.blend}")
    }
    if (before.blendSrcRGB != after.blendSrcRGB) {
        println("BLEND_SRC_RGB: ${before.blendSrcRGB} -> ${after.blendSrcRGB}")
    }
    if (before.blendDstRGB != after.blendDstRGB) {
        println("BLEND_DST_RGB: ${before.blendDstRGB} -> ${after.blendDstRGB}")
    }
    // Cull face
    if (before.cullFace != after.cullFace) {
        println("CULL_FACE: ${before.cullFace} -> ${after.cullFace}")
    }
    if (before.cullFaceMode != after.cullFaceMode) {
        println("CULL_FACE_MODE: ${before.cullFaceMode} -> ${after.cullFaceMode}")
    }
    if (before.frontFace != after.frontFace) {
        println("FRONT_FACE: ${before.frontFace} -> ${after.frontFace}")
    }
    // Stencil
    if (before.stencilTest != after.stencilTest) {
        println("STENCIL_TEST: ${before.stencilTest} -> ${after.stencilTest}")
    }
    if (!before.stencilFunc.contentEquals(after.stencilFunc)) {
        println("STENCIL_FUNC (func, ref, mask): ${before.stencilFunc.toList()} -> ${after.stencilFunc.toList()}")
    }
    // Rasterizer discard
    if (before.rasterizerDiscard != after.rasterizerDiscard) {
        println("RASTERIZER_DISCARD: ${before.rasterizerDiscard} -> ${after.rasterizerDiscard}")
    }
    // Shader program
    if (before.currentProgram != after.currentProgram) {
        println("CURRENT_PROGRAM: ${before.currentProgram} -> ${after.currentProgram}")
    }
    // Vertex Array Object
    if (before.vao != after.vao) {
        println("VERTEX_ARRAY_BINDING: ${before.vao} -> ${after.vao}")
    }
    // Array buffer bindings
    if (before.arrayBuffer != after.arrayBuffer) {
        println("ARRAY_BUFFER_BINDING: ${before.arrayBuffer} -> ${after.arrayBuffer}")
    }
    if (before.elementArrayBuffer != after.elementArrayBuffer) {
        println("ELEMENT_ARRAY_BUFFER_BINDING: ${before.elementArrayBuffer} -> ${after.elementArrayBuffer}")
    }
    // Transform Feedback binding
    if (before.tfBinding != after.tfBinding) {
        println("TRANSFORM_FEEDBACK_BINDING: ${before.tfBinding} -> ${after.tfBinding}")
    }
    if (before.tfBufferBinding != after.tfBufferBinding) {
        println("TRANSFORM_FEEDBACK_BUFFER_BINDING: ${before.tfBufferBinding} -> ${after.tfBufferBinding}")
    }
    // Framebuffer binding
    if (before.framebufferBinding != after.framebufferBinding) {
        println("FRAMEBUFFER_BINDING: ${before.framebufferBinding} -> ${after.framebufferBinding}")
    }
}

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
