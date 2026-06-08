#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define filterRadius MatrixPostData0.x

out vec4 fragColor;

void main() {
    vec4 color = vec4(0.0);
    vec2 tex_offset = 1.0 / textureSize(framebuffer, 0) * filterRadius;

    for (float x = -3.0; x <= 3.0; x += 1.0) {
        for (float y = -3.0; y <= 3.0; y += 1.0) {
            vec2 offset = vec2(x, y) * tex_offset;
            color += texture(framebuffer, fragTexCoord + offset);
        }
    }
    color /= 16.0;

    fragColor = color * 1.2;
}
