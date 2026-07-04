#version 410 core

layout (location = 0) in vec3 InPosition;
layout (location = 1) in vec3 InVelocity;
layout (location = 2) in vec3 InAcceleration;
layout (location = 3) in float InSpriteSize;
layout (location = 4) in float InScale;
layout (location = 5) in float InAge;
layout (location = 6) in float InLifetime;
layout (location = 7) in vec4 InColor;
layout (location = 8) in vec4 InOrientation;
layout (location = 9) in vec3 InAngularVelocity;

uniform float MinScaleFactor = 0.0;
uniform float MaxScaleFactor = 2.0;
uniform float VelocityThreshold = 1.0;

layout (location = 0) out vec3 OutPosition;
layout (location = 1) out vec3 OutVelocity;
layout (location = 2) out vec3 OutAcceleration;
layout (location = 3) out float OutSpriteSize;
layout (location = 4) out float OutScale;
layout (location = 5) out float OutAge;
layout (location = 6) out float OutLifetime;
layout (location = 7) out vec4 OutColor;
layout (location = 8) out vec4 OutOrientation;
layout (location = 9) out vec3 OutAngularVelocity;

void InitParticleStates() {
    OutPosition = InPosition;
    OutVelocity = InVelocity;
    OutAcceleration = InAcceleration;
    OutSpriteSize = InSpriteSize;
    OutScale = InScale;
    OutAge = InAge;
    OutLifetime = InLifetime;
    OutColor = InColor;
    OutOrientation = InOrientation;
    OutAngularVelocity = InAngularVelocity;
}

void ScaleSpriteSizeBySpeed() {
    float speed = length(InVelocity);

    // Normalize speed into [0, 1] range
    float normalizedSpeed = clamp(speed / VelocityThreshold, 0.0, 1.0);
    float scaleFactor = mix(MinScaleFactor, MaxScaleFactor, normalizedSpeed);
    OutScale = scaleFactor;
}

void main() {
    InitParticleStates();
    ScaleSpriteSizeBySpeed();
}