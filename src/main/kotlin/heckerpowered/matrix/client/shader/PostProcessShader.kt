package heckerpowered.matrix.client.shader

class PostProcessShader(vertex: String, fragment: String, uniform: Array<UniformProvider> = emptyArray()) :
    BlitShader(vertex, fragment, uniform) {
    var enabled = true
}