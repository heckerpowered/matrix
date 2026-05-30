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

layout (location = 0) out vec3 PassPosition;
layout (location = 1) out vec3 PassVelocity;
layout (location = 2) out vec3 PassAcceleration;
layout (location = 3) out float PassSpriteSize;
layout (location = 4) out float PassScale;
layout (location = 5) out float PassAge;
layout (location = 6) out float PassLifetime;
layout (location = 7) out vec4 PassColor;
layout (location = 8) out vec4 PassOrientation;
layout (location = 9) out vec3 PassAngularVelocity;

void InitParticleStates() {
    PassPosition = InPosition;
    PassVelocity = InVelocity;
    PassAcceleration = InAcceleration;
    PassSpriteSize = InSpriteSize;
    PassScale = InScale;
    PassAge = InAge;
    PassLifetime = InLifetime;
    PassColor = InColor;
    PassOrientation = InOrientation;
    PassAngularVelocity = InAngularVelocity;
}

void main() {
    InitParticleStates();
}