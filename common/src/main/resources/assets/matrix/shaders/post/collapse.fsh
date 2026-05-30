#version 330 core

in vec2 fragTexCoord;

uniform sampler2D depthAttachment;
uniform mat4 inverseViewMatrix;
uniform mat4 inverseProjectionMatrix;
uniform vec2 resolution;

uniform vec3 playerPosition;

uniform sampler2D noiseTexture;

uniform float dissolveFactor = 1.0;

out vec4 fragColor;

vec3 worldAbsolutePosition(vec2 uv, float depth) {
    // Convert UV coordinates and depth to clip space coordinates.
    // UV is mapped from [0,1] to [-1,1].
    // Depth is mapped from [0,1] (typically from a depth texture or depth buffer) to [-1,1] (clip space z).
    vec4 clipSpacePosition = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    // Convert clip space coordinates to view space coordinates.
    // inverse projection matrix handles the perspective projection, bu the result is still a homogeneous coordinate.
    // viewSpacePosition.w will contain the original view space depth information.
    vec4 viewSpacePosition = inverseProjectionMatrix * clipSpacePosition;

    // Perform perspective division on view space coordinates to get non-homogeneous view space coordinates.
    // This step is crucial as it converts from homogeneous to Cartesian coordinates.
    // Performing the division here is correct to obtain the actual view space position.
    vec3 nonHomogeneousViewSpacePosition = viewSpacePosition.xyz / viewSpacePosition.w;

    // Convert view space coordinates to world space coordinates.
    // Note: inverseViewMatrix is a 4x4 matrix and typically expects a vec4 input.
    // To transform correctly, we re-package nonHomogeneousViewSpacePosition into a vec4 with a w-component of 1, representing a point.
    vec4 worldSpacePosition = inverseViewMatrix * vec4(nonHomogeneousViewSpacePosition, 1.0);

    // Return the XYZ components of the world space position.
    // Theoretically, worldSpacePosition.w should be close to 1 (if inverseViewMatrix doesn't scale),
    // but to be safe, it's best to divide by it, though it will typically be 1.
    return worldSpacePosition.xyz / worldSpacePosition.w;
}

vec3 reconstructWorldNormal(vec2 uv, vec2 texelSize) {
    vec3 worldPos = worldAbsolutePosition(uv, texture(depthAttachment, uv).r);

    vec2 uvX = uv + vec2(texelSize.x, 0.0);
    vec2 uvY = uv + vec2(0.0, texelSize.y);

    vec3 worldPosX = worldAbsolutePosition(uvX, texture(depthAttachment, uvX).r);
    vec3 worldPosY = worldAbsolutePosition(uvY, texture(depthAttachment, uvY).r);

    vec3 tangent = worldPosX - worldPos;
    vec3 bitangent = worldPosY - worldPos;

    vec3 worldNormal = normalize(cross(tangent, bitangent));

    return worldNormal;
}

vec4 triplanarMapping(vec3 worldPosition, vec3 worldNormal, sampler2D textureSampler, float scale) {
    vec3 blendingWeights = abs(worldNormal);
    blendingWeights = max(blendingWeights, 0.0001);
    blendingWeights /= (blendingWeights.x + blendingWeights.y + blendingWeights.z);

    vec4 colorX = texture(textureSampler, worldPosition.yz * scale);
    vec4 colorY = texture(textureSampler, worldPosition.xz * scale);
    vec4 colorZ = texture(textureSampler, worldPosition.xy * scale);

    return colorX * blendingWeights.x +
    colorY * blendingWeights.y +
    colorZ * blendingWeights.z;
}

void main() {
    float nonLinearDepth = texture(depthAttachment, fragTexCoord).r;
    vec3 worldPosition = worldAbsolutePosition(fragTexCoord, nonLinearDepth);
    float dist = length(worldPosition - playerPosition);

    if (dist > 768.0) {
        discard;
    }

    vec3 worldNormal = reconstructWorldNormal(fragTexCoord, 1.0 / resolution);
    vec3 cameraToPixel = normalize(worldPosition - playerPosition);
    if (dot(worldNormal, cameraToPixel) > 0.0) {
        worldNormal = -worldNormal;
    }
    vec4 noise = triplanarMapping(worldPosition, worldNormal, noiseTexture, 1.0F / 128.0F);
    float alpha = step(noise.r, dissolveFactor);
    fragColor = vec4(vec3(0.1, 0.5, 1.0) * 4, alpha);
}