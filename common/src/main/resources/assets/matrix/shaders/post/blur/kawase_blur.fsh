#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define offset MatrixPostData0.xy

out vec4 fragColor;

void main() {
    vec2 texelSize = 1.0 / textureSize(framebuffer, 0);
    vec2 sampleOffset = offset * texelSize;
    vec4 color = texture(framebuffer, fragTexCoord);
    color += texture(framebuffer, fragTexCoord + vec2(sampleOffset.x, sampleOffset.y));
    color += texture(framebuffer, fragTexCoord + vec2(-sampleOffset.x, sampleOffset.y));
    color += texture(framebuffer, fragTexCoord + vec2(sampleOffset.x, -sampleOffset.y));
    color += texture(framebuffer, fragTexCoord + vec2(-sampleOffset.x, -sampleOffset.y));
    fragColor = color / 5;
}
