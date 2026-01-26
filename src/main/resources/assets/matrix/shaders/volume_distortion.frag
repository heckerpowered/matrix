#version 330 core

in vec2 fragTexCoord;

uniform sampler2D sceneColorTexture;
uniform sampler2D depthAttachment;

uniform mat4 viewProjectionMatrix;
uniform mat4 inverseViewMatrix;
uniform mat4 inverseProjectionMatrix;

uniform vec3 volumePosition;
uniform float volumeRadius;

uniform float grayscaleIntensity = 1.0;

out vec4 fragColor;

vec3 computeCameraWorldPosition()
{
    return inverseViewMatrix[3].xyz;
}

vec3 worldAbsolutePosition(vec2 uv, float depth)
{
    vec4 clipSpacePosition = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewSpacePosition = inverseProjectionMatrix * clipSpacePosition;
    vec3 nonHomogeneousViewSpacePosition = viewSpacePosition.xyz / viewSpacePosition.w;
    vec4 worldSpacePosition = inverseViewMatrix * vec4(nonHomogeneousViewSpacePosition, 1.0);
    return worldSpacePosition.xyz / worldSpacePosition.w;
}

vec3 computeViewRayDirectionWorld(vec2 uv)
{
    float depth = texture(depthAttachment, uv).r;
    vec3 worldPosition = worldAbsolutePosition(uv, depth);
    return normalize(worldPosition - computeCameraWorldPosition());
}

bool computeRaySphereIntersectionInterval(
    vec3 rayOriginWorldPosition,
    vec3 rayDirectionWorld,
    vec3 sphereCenterWorldPosition,
    float sphereRadiusWorldUnits,
out float enterDistanceWorldUnits,
out float exitDistanceWorldUnits
) {
    vec3 originToSphereCenter = rayOriginWorldPosition - sphereCenterWorldPosition;
    float projectionLength = dot(originToSphereCenter, rayDirectionWorld);
    float originToCenterDistanceSquared = dot(originToSphereCenter, originToSphereCenter);
    float discriminant = projectionLength * projectionLength - (originToCenterDistanceSquared - sphereRadiusWorldUnits * sphereRadiusWorldUnits);
    if (discriminant < 0.0) {
        return false;
    }
    float sqrtDiscriminant = sqrt(discriminant);
    float firstIntersection = -projectionLength - sqrtDiscriminant;
    float secondIntersection = -projectionLength + sqrtDiscriminant;
    enterDistanceWorldUnits = min(firstIntersection, secondIntersection);
    exitDistanceWorldUnits = max(firstIntersection, secondIntersection);
    return exitDistanceWorldUnits >= 0.0;
}

float computeSceneDepthDistanceWorldUnits(vec2 uv, vec3 cameraPosition)
{
    float depth = texture(depthAttachment, uv).r;
    vec3 sceneWorldPosition = worldAbsolutePosition(uv, depth);
    return length(sceneWorldPosition - cameraPosition);
}

vec3 computeGrayscaleColor(vec3 colorRgb)
{
    float grayscale = dot(colorRgb, vec3(0.299, 0.587, 0.114));
    return vec3(grayscale);
}

void main()
{
    vec3 cameraPosition = computeCameraWorldPosition();
    vec3 viewRayDirection = computeViewRayDirectionWorld(fragTexCoord);

    float enterDistance;
    float exitDistance;

    bool intersectsSphere =
    computeRaySphereIntersectionInterval(
        cameraPosition,
        viewRayDirection,
        volumePosition,
        volumeRadius,
        enterDistance,
        exitDistance
    );

    float sceneDepthDistance = computeSceneDepthDistanceWorldUnits(fragTexCoord, cameraPosition);
    float visibleMask = intersectsSphere && exitDistance > 0.0 && enterDistance < sceneDepthDistance ? 1.0 : 0.0;
    float visibleEnterDistance = max(enterDistance, 0.0);
    float travelDistanceWorldUnits = max(0.0, exitDistance - visibleEnterDistance);
    float volumeDensity = (1.0 - exp(-travelDistanceWorldUnits * 0.6)) * visibleMask;

    vec4 originalColor = texture(sceneColorTexture, fragTexCoord);
    vec3 grayscaleColor = computeGrayscaleColor(originalColor.rgb);
    vec3 finalColorRgb = mix(
        originalColor.rgb,
        grayscaleColor,
        volumeDensity * grayscaleIntensity
    );

    fragColor = vec4(finalColorRgb, originalColor.a);
}