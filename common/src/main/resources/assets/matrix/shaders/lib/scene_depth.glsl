vec3 worldPositionFromDepth(vec2 uv, float depth, mat4 inverseProjectionMatrix, mat4 inverseViewMatrix) {
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

vec3 worldPositionFromDepth(vec2 uv, float depth, mat4 inverseViewProjectionMatrix) {
    // NDC mapping: [0,1] -> [-1,1] (OpenGL convention)
    vec4 clipSpacePosition = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    // Single 4x4 multiply in homogeneous space
    vec4 worldHomogeneous = inverseViewProjectionMatrix * clipSpacePosition;

    // Perspective divide to get Cartesian world position
    return worldHomogeneous.xyz / worldHomogeneous.w;
}