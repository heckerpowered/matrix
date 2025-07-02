#version 410 core

uniform sampler2D noiseTexture;
uniform float dissolveFactor = 0.5;
uniform float emissiveRange = 0.05;
uniform vec4 emissiveColor = vec4(0.1, 0.5, 1.0, 1.0);
uniform float emissiveStrength = 15.0;
uniform float pixelStrength = 16.0;
uniform float detialStrength = 1;
uniform float time;
uniform vec2 resolution = vec2(1.0, 1.0);

layout (location = 0) out vec4 fragColor;

layout (location = 0) in vec2 fragTexCoord;
layout (location = 1) in vec4 vertexColor;

vec2 texCoord() {
    return fragTexCoord;
}

float pixelColor() {
    vec2 pixelTexCoord = ceil(texCoord() * pixelStrength) / pixelStrength;
    return texture(noiseTexture, pixelTexCoord + vec2(time * 0.1, time * 0.1)).b;
}

float pixelAnimation() {
    float pixelNoise = ceil(texCoord().r * pixelStrength) / pixelStrength;
    return pixelNoise - mix(-1.5, 1.5F, 1.0F - dissolveFactor);
}

float border() {
    vec4 normalColor = texture(noiseTexture, ceil(texCoord() * pixelStrength) / pixelStrength);
    vec4 offsetColor = texture(noiseTexture, ceil((texCoord() + 0.01) * pixelStrength) / pixelStrength);
    return (normalColor.b - offsetColor.b) * detialStrength;
}

float clamp(float minValue, float maxValue, float value) {
    return min(max(value, minValue), maxValue);
}

void main() {
    fragColor = vertexColor;

    float opacityMask = clamp(0, 1, (pixelColor() + border()) - pixelAnimation());
    fragColor.a *= ceil(opacityMask);
    fragColor.rgb = pow(1 - opacityMask, 10) * (emissiveColor.rgb * emissiveStrength);
}