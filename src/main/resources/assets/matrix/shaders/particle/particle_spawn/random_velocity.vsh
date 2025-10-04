// This is a template for a particle shader, no special effects.

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

uniform float time;
uniform vec2 speedRange = vec2(1.0);
uniform vec3 multiplier = vec3(1.0);

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

float hash(float x) {
    return fract(sin(x) * 43758.5453);
}

vec3 randomDirection(float seed) {
    float u = hash(seed);
    float v = hash(seed + 1);
    float theta = u * 2.0 * 3.1415926;
    float phi = acos(2.0 * v - 1.0);
    float x = sin(phi) * cos(theta);
    float y = sin(phi) * sin(theta);
    float z = cos(phi);
    return vec3(x, y, z);
}

float randomScalarInRange(float seed, float minValue, float maxValue) {
    float unitRandom = hash(seed);
    return mix(minValue, maxValue, unitRandom);
}

void main() {
    InitParticleStates();

    vec3 Velocity = randomDirection(gl_VertexID + time);
    float randomSpeed = randomScalarInRange(gl_VertexID + 114.514, speedRange.x, speedRange.y);
    OutVelocity += Velocity * randomSpeed * multiplier;
}
