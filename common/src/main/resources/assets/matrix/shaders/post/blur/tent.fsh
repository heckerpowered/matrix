#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define lod MatrixPostData0.x

out vec4 fragColor;

const vec3 tentKernel[9] = vec3[9](
vec3(-1, 1, 1), vec3(0, 1, 2), vec3(1, 1, 1),
vec3(-1, 0, 2), vec3(0, 0, 4), vec3(1, 0, 2),
vec3(-1, -1, 1), vec3(0, -1, 2), vec3(1, -1, 1)
);

// Function to calculate perceived brightness (luminance)
float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    vec4 color = vec4(0);
    // 1.21 sampled the full mip chain with FULL-RES texel offsets (the +-1 tent collapsed
    // toward a bilinear fetch at deep lods — all spread came from the mip cascade). The
    // 26.2 port feeds single-level views, whose textureSize is the LEVEL's size, so scale
    // the offsets by exp2(-lod) to keep the original full-res-texel basis.
    vec2 texelSize = exp2(-lod) / textureSize(framebuffer, 0);
    float weight = 0;

    for (int i = 0;i < 9; ++i) {
        vec3 kernel = tentKernel[i];
        vec4 sampledColor = textureLod(framebuffer, fragTexCoord + kernel.xy * texelSize, lod);
        float l = kernel.z;/// (1 + luminance(sampledColor.rgb));
        color += sampledColor * l;
        weight += l;
    }

    color /= weight;
    fragColor = color;
}
