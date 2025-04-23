#version 330 core

in vec2 fragTexCoord;

uniform sampler2D noiseTexture;
uniform float time;
uniform float innerRadius = 0.8;
uniform float outerRadius = 0.9;
uniform float feather = 0.02;

out vec4 fragColor;

/**
 * Computes an exponential falloff based on distance (depth) and density.
 *
 * Two modes are supported:
 *  - Standard exponential decay:         f = exp(–distance * density)
 *  - Squared-input exponential decay:    f = exp(–(distance * density)²)
 *
 * @param depth    Depth value (must be ≥ 0; negative values are clamped to 0).
 * @param density     Controls how quickly the value decays with distance.
 * @param useSquared  If true, squares (distance * density) to produce a sharper falloff.
 * @return            A float in (0, 1], representing decayed intensity.
 */
float exponentialDensity(float depth, float density, bool useSquared)
{
    depth = max(depth, 0.0);
    float d = depth * density;
    if (useSquared) {
        d = d * d;
    }

    return exp(-d);
}

/**
 * Generates a radial gradient with exponential falloff from a given center.
 *
 * This is similar to Unreal Engine's RadialGradientExponential node.
 *
 * @param uv             The input UV coordinate (in [0, 1] space).
 * @param centerPosition The center of the gradient, also in [0, 1] space.
 * @param radius         The radius of the gradient area; outside of this, values decay.
 * @param density        Controls the steepness of the falloff (higher = sharper edge).
 * @param invertDensity  If true, the gradient grows outward from the center instead of inward.
 *
 * @return               A float value between 0 and 1 representing the gradient strength.
 */
float radialGradientExponential(vec2 uv, vec2 centerPosition, float radius, float density, bool invertDensity)
{
    float normalizedDistance = distance(uv, centerPosition) / radius;
    float depth = invertDensity ? normalizedDistance : 1.0 - normalizedDistance;
    float result = exponentialDensity(depth, density, true);
    return invertDensity ? result : 1.0 - result;
}

/**
 * Converts 2D UV coordinates to radial polar coordinates.
 *
 * @param uv                       The input UV coordinates in [0, 1] space.
 * @param swizzleCoordinateOutput If true, returns (angle, radius); otherwise (radius, angle).
 *                                This is useful for matching Unreal Engine's VectorToRadial output.
 * @return                        A vec2 where x = radius and y = angle (or swizzled).
 */
vec2 vectorToRadialValue(vec2 uv, bool swizzleCoordinateOutput)
{
    vec2 centeredUV = uv * 2.0 - 1.0;
    centeredUV.y *= -1.0;

    float radius = length(centeredUV);
    float angle = atan(centeredUV.y, centeredUV.x) / 6.283185;// 2π = 6.283185

    // Output (radius, angle) or (angle, radius) depending on swizzle
    return swizzleCoordinateOutput ? vec2(angle, radius) : vec2(radius, angle);
}

void main() {
    vec2 radialUV = vectorToRadialValue(fragTexCoord, true);
    vec2 offset = vec2(time * 0.05, time * -0.1);

    vec4 texA = texture(noiseTexture, radialUV * vec2(2.0, 1.0) + offset);
    vec4 texB = texture(noiseTexture, radialUV * vec2(3.0, 1.0) + offset);
    vec4 blendedTex = (texA + texB) * 0.5;

    float gradientInner = radialGradientExponential(fragTexCoord, vec2(0.5), innerRadius, 2.333, false);
    float gradientOuter = radialGradientExponential(fragTexCoord, vec2(0.5), outerRadius, 2.333, false);

    float ringMask = smoothstep(gradientInner - feather, gradientInner + feather, blendedTex.r) - smoothstep(gradientOuter - feather, gradientOuter + feather, blendedTex.r);
    float opacityMask = radialGradientExponential(fragTexCoord, vec2(0.5), 0.4, 1.0, false);
    float alpha = 1.0 - step(blendedTex.r, gradientInner) + ringMask * opacityMask;
    if (alpha == .0) {
        discard;
    }
    fragColor = vec4(ringMask, ringMask, ringMask, 1.0);
}