#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
    vec4 MatrixPostData4;
    vec4 MatrixPostData5;
    vec4 MatrixPostData6;
    vec4 MatrixPostData7;
    vec4 MatrixPostData8;
    vec4 MatrixPostData9;
    vec4 MatrixPostData10;
    vec4 MatrixPostData11;
    vec4 MatrixPostData12;
    vec4 MatrixPostData13;
};

#define direction MatrixPostData0.xy
#define kernelSize int(MatrixPostData0.z)

float kernelAt(int index) {
    int block = index / 4;
    int component = index - block * 4;
    if (block == 0) return MatrixPostData1[component];
    if (block == 1) return MatrixPostData2[component];
    if (block == 2) return MatrixPostData3[component];
    if (block == 3) return MatrixPostData4[component];
    if (block == 4) return MatrixPostData5[component];
    if (block == 5) return MatrixPostData6[component];
    if (block == 6) return MatrixPostData7[component];
    if (block == 7) return MatrixPostData8[component];
    if (block == 8) return MatrixPostData9[component];
    if (block == 9) return MatrixPostData10[component];
    if (block == 10) return MatrixPostData11[component];
    if (block == 11) return MatrixPostData12[component];
    return MatrixPostData13[component];
}

out vec4 fragColor;

void main() {
    vec4 originalColor = texture(framebuffer, fragTexCoord);
    if (kernelSize == 0) {
        fragColor = originalColor;
        return;
    }
    vec4 color = originalColor * kernelAt(0);
    vec2 offset = direction / vec2(textureSize(framebuffer, 0));

    for (int i = 1; i <= kernelSize; ++i) {
        vec2 subOffset = offset * float(i);
        float weight = kernelAt(i);
        color += texture(framebuffer, fragTexCoord + subOffset) * weight;
        color += texture(framebuffer, fragTexCoord - subOffset) * weight;
    }

    fragColor = color;
}
