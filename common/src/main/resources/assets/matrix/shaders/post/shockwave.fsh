#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform sampler2D depthAttachment;

layout(std140) uniform MatrixPostUniforms {
    mat4 inverseProjectionMatrix;
    mat4 inverseViewMatrix;
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
};

#define wavePosition MatrixPostData0.xyz
#define waveRadius MatrixPostData0.w
#define waveColor MatrixPostData1
#define waveSize MatrixPostData2.x

out vec4 fragColor;

vec3 worldAbsolutePosition(vec2 uv, float depth) {
    // Convert UV coordinates and depth to clip space coordinates.
    // UV is mapped from [0,1] to [-1,1].
    // Depth is mapped from [0,1] (typically from a depth texture or depth buffer) to [-1,1] (clip space z).
    // 26.2 renders with zero-to-one depth on both backends (glClipControl GL_ZERO_TO_ONE /
    // Vulkan native); the inverse projection encodes that convention, so use depth directly.
    vec4 clipSpacePosition = vec4(uv * 2.0 - 1.0, depth, 1.0);

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

void main() {
    vec4 sceneColor = texture(framebuffer, fragTexCoord);
    float depth = texture(depthAttachment, fragTexCoord).r;
    vec3 worldPosition = worldAbsolutePosition(fragTexCoord, depth);
    float dist = length(worldPosition - wavePosition);

    // dist > waveRadius && dist < (waveRadius + waveSize)
    float insideWave = step(waveRadius, dist) * step(dist, waveRadius + waveSize);
    fragColor = sceneColor + waveColor * insideWave;
}
