#version 410 core

uniform sampler2D noiseTexture;
uniform sampler2D normalTexture;

uniform float dissolveFactor = 0.5;
uniform float emissiveRange = 0.05;
uniform vec4 emissiveColor = vec4(0, 0.5, 1.0, 1.0);
uniform float pixelStrength = 100.0;
uniform float detialStrength = 1;

layout (location = 0) out vec4 fragColor;

layout (location = 0) in vec2 fragTexCoord;

vec2 texCoord() {
    return fragTexCoord;
}

float pixelColor() {
    vec2 pixelTexCoord = ceil(texCoord() * pixelStrength) / pixelStrength;
    return texture(noiseTexture, pixelTexCoord).b;
}

float pixelAnimation() {
    float pixelNoise = ceil(texCoord().x * pixelStrength) / pixelStrength;
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
    fragColor = texture(normalTexture, fragTexCoord);
    if (fragColor.a < 0.1) {
        discard;
    }

    float opacityMask = clamp(0, 1, (pixelColor() + border()) - pixelAnimation());
    fragColor.a *= ceil(opacityMask);
    fragColor.rgb = pow(1 - opacityMask, 10) * emissiveColor.rgb;
}