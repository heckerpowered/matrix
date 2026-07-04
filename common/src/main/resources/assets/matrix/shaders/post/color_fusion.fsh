#version 330 core

in vec2 fragTexCoord;

uniform sampler2D primaryFramebuffer;
uniform sampler2D secondaryFramebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define colorMultiplier MatrixPostData0

out vec4 fragColor;

void main() {
    vec4 primaryFramebufferColor = texture(primaryFramebuffer, fragTexCoord);
    vec4 secondaryFramebufferColor = texture(secondaryFramebuffer, fragTexCoord);
    vec3 rgbColor = primaryFramebufferColor.rgb + secondaryFramebufferColor.rgb;
    // float alpha = (primaryFramebufferColor.a + secondaryFramebufferColor.a) / 2;
    fragColor = (primaryFramebufferColor + secondaryFramebufferColor) * colorMultiplier;
    // fragColor.a = alpha;
}
