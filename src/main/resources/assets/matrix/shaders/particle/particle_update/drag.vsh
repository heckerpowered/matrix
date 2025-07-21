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

uniform float DeltaTime;
uniform float MinDrag = 0.8;
uniform float MaxDrag = 1.2;

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

float RandomFloat(uint vertexId, float minVal, float maxVal) {
    // Hash vertexId → uint
    uint seed = vertexId;
    seed = (seed ^ 61u) ^ (seed >> 16);
    seed *= 9u;
    seed = seed ^ (seed >> 4);
    seed *= 0x27d4eb2du;
    seed = seed ^ (seed >> 15);

    // Convert to float in [0,1)
    float normalized = float(seed & 0x00FFFFFFu) / float(0x01000000u);// 24-bit to [0,1)
    return normalized;
}

float RandomRangeFloat(uint vertexId, float minVal, float maxVal) {
    float normalized = RandomFloat(vertexId, 0.0, 1.0);
    return mix(minVal, maxVal, normalized);
}

vec3 ApplyLinearDrag(vec3 velocity, float drag, float deltaTime) {
    return velocity * max(0.0, 1.0 - drag * deltaTime);
}

void main() {
    InitParticleStates();

    float drag = RandomRangeFloat(gl_VertexID, MinDrag, MaxDrag);
    OutVelocity = ApplyLinearDrag(InVelocity, drag, DeltaTime);
}