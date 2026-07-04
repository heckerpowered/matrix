#version 330 core

in vec2 fragTexCoord;

uniform sampler2D noiseTexture;
uniform sampler2D normalTexture;

layout(std140) uniform MatrixPostUniforms {
    vec4 dissolveParams0;
    vec4 dissolveEmissiveColor;
};

#define dissolveFactor dissolveParams0.x
#define emissiveRange dissolveParams0.y
#define pixelStrength dissolveParams0.z
#define detialStrength dissolveParams0.w
#define emissiveColor dissolveEmissiveColor

out vec4 fragColor;

vec2 texCoord() {
    return fragTexCoord;
}

float pixelColor() {
    vec2 pixelTexCoord = ceil(texCoord() * pixelStrength) / pixelStrength;
    return texture(noiseTexture, pixelTexCoord).b;
}

float pixelAnimation() {
    float pixelNoise = ceil(texCoord().x * pixelStrength) / pixelStrength;
    return pixelNoise - mix(-1.5, 1.5, 1.0 - dissolveFactor);
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
    fragColor = texture(normalTexture, fragTexCoord);
    if (fragColor.a < 0.1) {
        discard;
    }

    float opacityMask = clampRange(0.0, 1.0, (pixelColor() + border()) - pixelAnimation());
    fragColor.a *= ceil(opacityMask);
    fragColor.rgb = pow(1.0 - opacityMask, 10.0) * emissiveColor.rgb;
}
