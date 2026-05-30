package heckerpowered.render

data class RenderPassDesc(
    val colorAttachments: List<GpuTexture>,
    val depthAttachment: GpuTexture? = null,
    val shouldClearColor: Boolean = true,
    val shouldClearDepth: Boolean = true
)