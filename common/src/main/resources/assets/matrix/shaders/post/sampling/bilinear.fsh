#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define sourceResolution MatrixPostData0.xy
#define targetResolution MatrixPostData0.zw

out vec4 fragColor;

void main() {
    vec2 resolutionScale = sourceResolution / targetResolution;
    vec2 pixelSize = 1.0 / targetResolution / resolutionScale;
    vec2 sourceTexCoord = fragTexCoord;

    vec4 color = texture(framebuffer, sourceTexCoord);
    color += texture(framebuffer, sourceTexCoord + vec2(pixelSize.x, 0));
    color += texture(framebuffer, sourceTexCoord + vec2(0, pixelSize.y));
    color += texture(framebuffer, sourceTexCoord + pixelSize);
    color /= 4;

    fragColor = color;
}
