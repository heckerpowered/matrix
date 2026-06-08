#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define strength MatrixPostData0.x
#define samples int(MatrixPostData0.y)

out vec4 fragColor;

const vec2 center = vec2(0.5, 0.5);

void main() {
    vec4 color = vec4(0);

    vec2 direction = (center - fragTexCoord) * strength * 0.01;
    for (int i = 0; i < samples; ++i) {
        color += texture(framebuffer, fragTexCoord + direction * float(i)) / float(samples);
    }

    fragColor = color;
}
