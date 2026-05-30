#version 410 core

// Vertex Shader for GPU Particle Initialization
//
// This vertex shader initializes the state of all particles in parallel.
// All particle states will be set to the values specified by the uniform variables.
// The initialized particle states must be captured using Transform Feedback.
// This shader must not be used for rasterization.

// Particle input:
// layout(location = 0) in vec3 InPosition;
// layout(location = 1) in vec3 InVelocity;
// layout(location = 2) in vec3 InAcceleration;
// layout(location = 3) in float InSpriteSize;
// layout(location = 4) in float InScale;
// layout(location = 5) in float InAge;
// layout(location = 6) in float InLifetime;
// layout(location = 7) in vec4 InColor;
// layout(location = 8) in vec4 InOrientation;
// layout(location = 9) in vec3 InAngularVelocity;
//
// Particle output:
// layout(location = 0) out vec3 OutPosition;
// layout(location = 1) out vec3 OutVelocity;
// layout(location = 2) out vec3 OutAcceleration;
// layout(location = 3) out float OutSpriteSize;
// layout(location = 4) out float OutScale;
// layout(location = 5) out float OutAge;
// layout(location = 6) out float OutLifetime;
// layout(location = 7) out vec4 OutColor;
// layout(location = 8) out vec4 OutOrientation;
// layout(location = 9) out vec3 OutAngularVelocity;

// uniform vec3 Position;
// uniform vec3 Velocity;
// uniform vec3 Acceleration;
// uniform float SpriteSize;
// uniform float Scale;
// uniform float Age;
// uniform float Lifetime;
// uniform vec4 Color;
// uniform vec4 Orientation;
// uniform vec3 AngularVelocity;

layout (std140) uniform ParticleState {
    uniform vec3 Position;
    uniform vec3 Velocity;
    uniform vec3 Acceleration;
    uniform float SpriteSize;
    uniform float Scale;
    uniform float Age;
    uniform float Lifetime;
    uniform vec4 Color;
    uniform vec4 Orientation;
    uniform vec3 AngularVelocity;
};

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

void main() {
    OutPosition = Position;
    OutVelocity = Velocity;
    OutAcceleration = Acceleration;
    OutSpriteSize = SpriteSize;
    OutScale = Scale;
    OutAge = Age;
    OutLifetime = Lifetime;
    OutColor = Color;
    OutOrientation = Orientation;
    OutAngularVelocity = AngularVelocity;
}