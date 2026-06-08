#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define primaryLevelOfDetail MatrixPostData0.x
#define secondaryLevelOfDetail MatrixPostData0.y
#define alpha MatrixPostData0.z

out vec4 fragColor;

void main() {
    vec4 primaryColor = textureLod(framebuffer, fragTexCoord, primaryLevelOfDetail);
    vec4 secondaryColor = textureLod(framebuffer, fragTexCoord, secondaryLevelOfDetail);
    vec4 mixColor = mix(primaryColor, secondaryColor, alpha);
    fragColor = mixColor;
}
