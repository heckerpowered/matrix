#version 410 core

uniform sampler2D noiseTexture;

layout(std140) uniform MatrixPostUniforms {
    vec4 dissolveParams0;
    vec4 dissolveParams1;
    vec4 dissolveEmissiveColor;
};

#define dissolveFactor dissolveParams0.x
#define emissiveRange dissolveParams0.y
#define emissiveStrength dissolveParams0.z
#define pixelStrength dissolveParams0.w
#define detialStrength dissolveParams1.x
#define time dissolveParams1.y
#define resolution dissolveParams1.zw
#define emissiveColor dissolveEmissiveColor

layout (location = 0) out vec4 fragColor;

layout (location = 0) in vec2 fragTexCoord;
layout (location = 1) in vec4 vertexColor;

vec2 texCoord() {
    vec2 uv = fragTexCoord - 0.5;
    uv.y *= resolution.y / resolution.x;
    return uv + 0.5;
}

float pixelColor() {
    vec2 pixelTexCoord = texCoord();// ceil(texCoord() * pixelStrength) / pixelStrength;
    return texture(noiseTexture, pixelTexCoord + vec2(time * 0.1, time * 0.1)).b;
}

float pixelAnimation() {
    float pixelNoise = texCoord().r;// ceil(texCoord().r * pixelStrength) / pixelStrength;
    return pixelNoise - mix(1.5, -1.5F, dissolveFactor);
}

float border() {
    vec4 normalColor = texture(noiseTexture, ceil(texCoord() * pixelStrength) / pixelStrength);
    vec4 offsetColor = texture(noiseTexture, ceil((texCoord() + 0.01) * pixelStrength) / pixelStrength);
    return (normalColor.b - offsetColor.b) * detialStrength;
}

float clampRange(float minValue, float maxValue, float value) {
    return min(max(value, minValue), maxValue);
}

void main() {
    fragColor = vertexColor;

    float opacityMask = clampRange(0.0, 1.0, pixelColor() - pixelAnimation());
    fragColor.a *= ceil(opacityMask);
    fragColor.rgb = pow(1 - opacityMask, 10) * (emissiveColor.rgb * emissiveStrength);
}
