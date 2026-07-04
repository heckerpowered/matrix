#version 330 core

in vec2 fragTexCoord;

uniform sampler2D sceneColorTexture;
uniform sampler2D depthAttachment;

layout(std140) uniform MatrixPostUniforms {
    mat4 inverseViewMatrix;
    mat4 inverseProjectionMatrix;
    vec4 volumeParams0;
    vec4 volumeParams1;
};

#define volumePosition volumeParams0.xyz
#define volumeRadius volumeParams0.w
#define grayscaleIntensity volumeParams1.x
#define emissiveStrength volumeParams1.y

out vec4 fragColor;

vec3 computeCameraWorldPosition()
{
    return inverseViewMatrix[3].xyz;
}

vec3 worldAbsolutePosition(vec2 uv, float depth)
{
    // 26.2 renders with zero-to-one depth on both backends (glClipControl GL_ZERO_TO_ONE /
    // Vulkan native); the inverse projection encodes that convention, so use depth directly.
    vec4 clipSpacePosition = vec4(uv * 2.0 - 1.0, depth, 1.0);
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

vec3 remapColorToFixedBrightness(vec3 colorRgb, float targetBrightness)
{
    float currentBrightness = dot(colorRgb, vec3(0.2126, 0.7152, 0.0722));
    float safeBrightness = max(currentBrightness, 1e-4);
    float brightnessScale = targetBrightness / safeBrightness;
    return colorRgb * brightnessScale;
}

float computeSphereEdgeGlowFactorFromSilhouette(float silhouetteSignedDistanceWorldUnits, float edgeWidthWorldUnits)
{
    return 1.0 - smoothstep(0.0, edgeWidthWorldUnits, -silhouetteSignedDistanceWorldUnits);
}

float computeSphereSilhouetteSignedDistanceWorldUnits(
    vec3 cameraWorldPosition,
    vec3 viewRayDirectionWorld,
    vec3 sphereCenterWorldPosition,
    float sphereRadiusWorldUnits
)
{
    vec3 cameraToSphereCenter = sphereCenterWorldPosition - cameraWorldPosition;
    float cameraToCenterDistance = length(cameraToSphereCenter);
    vec3 cameraToCenterDirection = cameraToSphereCenter / cameraToCenterDistance;

    float perpendicularDistanceToRay = length(cross(viewRayDirectionWorld, cameraToCenterDirection)) * cameraToCenterDistance;
    return perpendicularDistanceToRay - sphereRadiusWorldUnits;
}

void main()
{
    vec3 cameraPosition = computeCameraWorldPosition();
    vec3 viewRayDirection = computeViewRayDirectionWorld(fragTexCoord);

    float enterDistance;
    float exitDistance;

    bool intersectsSphere = computeRaySphereIntersectionInterval(
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

    float silhouetteSignedDistanceWorldUnits = computeSphereSilhouetteSignedDistanceWorldUnits(
        cameraPosition,
        viewRayDirection,
        volumePosition,
        volumeRadius
    );

    float edgeGlowFactor = computeSphereEdgeGlowFactorFromSilhouette(
        silhouetteSignedDistanceWorldUnits,
        volumeRadius * 0.01
    ) * visibleMask;
    vec3 edgeEnhancedColor = remapColorToFixedBrightness(originalColor.rgb, edgeGlowFactor * emissiveStrength);

    vec3 grayscaleColor = computeGrayscaleColor(originalColor.rgb);
    vec3 colorWithInnerGrayscale = mix(originalColor.rgb, grayscaleColor, volumeDensity * grayscaleIntensity);
    vec3 finalColorRgb = mix(colorWithInnerGrayscale, edgeEnhancedColor, edgeGlowFactor);

    fragColor = vec4(finalColorRgb, originalColor.a);
}
