#version 330 core

in vec2 fragTexCoord;

uniform sampler2D hdrScene;
uniform float exposure = 1.0;          // Set from application. Interpreted as linear scale.
uniform float exposureEv = 0;          // Optional: exposure in EV. If unused, set to 0.

out vec4 fragColor;

const mat3 ACESInputMatrix = mat3(
0.59719, 0.35458, 0.04823,
0.07600, 0.90834, 0.01566,
0.02840, 0.13383, 0.83777
);

const mat3 ACESOutputMatrix = mat3(
1.60475, -0.53108, -0.07367,
-0.10208, 1.10813, -0.00605,
-0.00327, -0.07276, 1.07602
);

vec3 applyRrtAndOdtFit(vec3 colorValue) {
    // Numerically-stable variant with a small floor on the denominator
    const float epsilon = 1e-5;
    vec3 numerator = colorValue * (colorValue + 0.0245786) - 0.000090537;
    vec3 denominator = colorValue * (0.983729 * colorValue + 0.4329510) + 0.238081;
    return numerator / max(denominator, vec3(epsilon));
}

vec3 applyAcesFilmic(vec3 colorLinear) {
    vec3 safeLinear = max(colorLinear, 0.0);
    vec3 acesColor = ACESInputMatrix * safeLinear;
    vec3 fitted = applyRrtAndOdtFit(acesColor);
    vec3 outputLinear = ACESOutputMatrix * fitted;
    return clamp(outputLinear, 0.0, 1.0);
}

vec3 convertLinearToSrgb(vec3 linearColor) {
    // IEC 61966-2-1 standard OETF
    vec3 x = clamp(linearColor, 0.0, 1.0);
    bvec3 isLow = lessThanEqual(x, vec3(0.0031308));
    vec3 lowPart = x * 12.92;
    vec3 highPart = 1.055 * pow(x, vec3(1.0 / 2.4)) - 0.055;
    return mix(highPart, lowPart, vec3(isLow));
}

void main() {
    vec3 hdrColor = texture(hdrScene, fragTexCoord).rgb;

    // Combine linear scale and EV scale; exp2(EV) converts stops to multiplier
    float exposureScale = exposure * exp2(exposureEv);
    vec3 exposedColor = hdrColor * exposureScale * 1.45;

    vec3 mappedLinear = applyAcesFilmic(exposedColor);

    // If GL_FRAMEBUFFER_SRGB is enabled, skip the conversion below
    vec3 srgbColor = convertLinearToSrgb(mappedLinear);

    fragColor = vec4(srgbColor, 1.0);
}