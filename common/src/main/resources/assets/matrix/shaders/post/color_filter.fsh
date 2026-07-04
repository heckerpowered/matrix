#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define color MatrixPostData0

out vec4 fragColor;

void main() {
    vec4 framebufferColor = texture(framebuffer, fragTexCoord);
    fragColor = framebufferColor * color;
}
