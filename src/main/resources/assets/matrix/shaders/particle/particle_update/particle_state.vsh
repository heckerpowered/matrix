#version 410 core

layout(location = 0) in vec3 InPosition;
layout(location = 1) in vec3 InVelocity;
layout(location = 2) in vec3 InAcceleration;
layout(location = 3) in float InSpriteSize;
layout(location = 4) in float InScale;
layout(location = 5) in float InAge;
layout(location = 6) in float InLifetime;
layout(location = 7) in vec4 InColor;
layout(location = 8) in vec4 InOrientation;
layout(location = 9) in vec3 InAngularVelocity;

uniform float DeltaTime;

layout(location = 0) out vec3 OutPosition;
layout(location = 1) out vec3 OutVelocity;
layout(location = 2) out vec3 OutAcceleration;
layout(location = 3) out float OutSpriteSize;
layout(location = 4) out float OutScale;
layout(location = 5) out float OutAge;
layout(location = 6) out float OutLifetime;
layout(location = 7) out vec4 OutColor;
layout(location = 8) out vec4 OutOrientation;
layout(location = 9) out vec3 OutAngularVelocity;

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

void main() {
    InitParticleStates();

    vec3 NewVelocity = InVelocity + InAcceleration * DeltaTime;
    vec3 NewPosition = InPosition + NewVelocity * DeltaTime;
    float NewAge = InAge + DeltaTime;

    OutPosition = NewPosition;
    OutVelocity = NewVelocity;
    OutAge = NewAge;
}
