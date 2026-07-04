#version 410 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D image;

layout(std140) uniform MatrixPostUniforms {
    vec4 blurParams0;
};

#define radius blurParams0.x

const vec2 BlurDir = vec2(1.2, 0.8);

void main() {
    vec2 texelSize = vec2(1.0) / textureSize(image, 0).xy;

    float opacity = 1.0;

    vec4 origColor = texture(image, fragTexCoord);

    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;
    float totalAlpha = 0.0;
    float totalSamples = 0.0;
    for (float r = -radius; r <= radius; r += 1.0) {
        vec4 sampleValue = texture(image, fragTexCoord + texelSize * r * BlurDir);

        // Accumulate average alpha
        totalAlpha = totalAlpha + sampleValue.a;
        totalSamples = totalSamples + 1.0;

        // Accumulate smoothed blur
        float strength = 1.0 - abs(r / radius);
        totalStrength = totalStrength + strength;
        blurred = blurred + sampleValue;
    }

    fragColor = vec4(mix(origColor.rgb, blurred.rgb / (radius * 2.0 + 1.0), opacity), 1.0);
}