#version 330 core

in vec2 fragTexCoord;

uniform sampler2D opacityMask;
uniform sampler2D noiseTexture;

layout(std140) uniform MatrixPostUniforms {
    vec4 grainParams;
};

#define grainStrength grainParams.x

out vec4 fragColor;

void main() {
    vec4 opacity = texture(opacityMask, fragTexCoord);
    if (opacity.a == 0.0) {
        discard;
    }
    fragColor = texture(noiseTexture, fragTexCoord) * grainStrength;
}
