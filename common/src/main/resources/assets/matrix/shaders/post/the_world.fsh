#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout (std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define grayscaleIntensity MatrixPostData0.x

out vec4 fragColor;

void main() {
    vec4 framebufferColor = texture(framebuffer, fragTexCoord);
    float grayscale = dot(framebufferColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 color = mix(framebufferColor.rgb, vec3(grayscale), grayscaleIntensity);
    fragColor = vec4(color, framebufferColor.a);
}
