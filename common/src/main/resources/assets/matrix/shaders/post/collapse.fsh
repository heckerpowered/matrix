#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform sampler2D depthAttachment;

layout(std140) uniform MatrixPostUniforms {
    mat4 inverseProjectionMatrix;
    mat4 inverseViewMatrix;
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
};

#define playerPosition MatrixPostData0.xyz
#define dissolveFactor MatrixPostData0.w
#define resolution MatrixPostData1.xy

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

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float valueNoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float n000 = hash13(i + vec3(0.0, 0.0, 0.0));
    float n100 = hash13(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash13(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash13(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash13(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash13(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash13(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash13(i + vec3(1.0, 1.0, 1.0));

    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    float nxy0 = mix(nx00, nx10, f.y);
    float nxy1 = mix(nx01, nx11, f.y);
    return mix(nxy0, nxy1, f.z);
}

void main() {
    vec4 sceneColor = texture(framebuffer, fragTexCoord);
    float nonLinearDepth = texture(depthAttachment, fragTexCoord).r;
    vec3 worldPosition = worldAbsolutePosition(fragTexCoord, nonLinearDepth);
    float dist = length(worldPosition - playerPosition);

    if (dist > 768.0) {
        fragColor = sceneColor;
        return;
    }

    vec3 worldNormal = reconstructWorldNormal(fragTexCoord, 1.0 / resolution);
    vec3 cameraToPixel = normalize(worldPosition - playerPosition);
    if (dot(worldNormal, cameraToPixel) > 0.0) {
        worldNormal = -worldNormal;
    }
    float noise = valueNoise(worldPosition / 16.0 + worldNormal * 2.0);
    float alpha = step(noise, dissolveFactor);
    vec3 collapseColor = vec3(0.1, 0.5, 1.0) * 4.0;
    fragColor = sceneColor + vec4(collapseColor * alpha, 0.0);
}
