#version 410 core

uniform sampler2D noiseTexture;
uniform float dissolveFactor;
uniform float emissiveRange = 0.05;
uniform vec4 emissiveColor = vec4(0, 0.5, 1.0, 1.0);
uniform float pixelStrength = 16.0;
uniform float detialStrength = 1.0;

layout (location = 3) in vec2 fragTexCoord;
layout (location = 4) in vec4 vertexColor;

out vec4 fragColor;

float pixelColor() {
    vec2 pixelTexCoord = ceil(fragTexCoord * pixelStrength) / pixelStrength;
    return texture(noiseTexture, pixelTexCoord).g;
}

float pixelAnimation() {
    float pixelNoise = ceil(fragTexCoord.g * pixelStrength) / pixelStrength;
    return pixelNoise - mix(-1.5, 1.5, 1 - dissolveFactor);
}

float border() {
    vec4 normalColor = texture(noiseTexture, ceil(fragTexCoord * pixelStrength) / pixelStrength);
    vec4 offsetColor = texture(noiseTexture, ceil((fragTexCoord + 0.01) * pixelStrength) / pixelStrength);
    return (normalColor.g - offsetColor.g) * detialStrength;
}

void main() {
    fragColor = vertexColor;

    float opacityMask = clamp(0, 1, (pixelColor() + border()) - pixelAnimation());
    fragColor.a *= ceil(opacityMask);
    fragColor.rgb = pow(1 - opacityMask, 10) * emissiveColor.rgb;

    // if (opacityNoise < min(dissolveFactor + emissiveRange, 0.8)) {
    //     fragColor = emissiveColor;
    // }
    // if (opacityNoise <= dissolveFactor) {
    //     discard;
    // }
}