#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define edgeThreshold MatrixPostData0.x
#define edgeColor MatrixPostData1

out vec4 fragColor;

void main()
{
    vec4 color = texture(framebuffer, fragTexCoord);

    vec3 left = texture(framebuffer, fragTexCoord + vec2(-1.0, 0.0) / textureSize(framebuffer, 0)).rgb;
    vec3 right = texture(framebuffer, fragTexCoord + vec2(1.0, 0.0) / textureSize(framebuffer, 0)).rgb;
    vec3 up = texture(framebuffer, fragTexCoord + vec2(0.0, 1.0) / textureSize(framebuffer, 0)).rgb;
    vec3 down = texture(framebuffer, fragTexCoord + vec2(0.0, -1.0) / textureSize(framebuffer, 0)).rgb;

    float edge = length(left + right + up + down - 4.0 * color.rgb);

    if (edge > edgeThreshold) {
        fragColor = edgeColor;
    } else {
        fragColor = color;
    }
}
