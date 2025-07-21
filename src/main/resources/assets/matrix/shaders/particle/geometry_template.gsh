#version 410 core

layout (points) in;
layout (points, max_vertices = 1) out;

layout (location = 0) in vec3 PassPosition[];
layout (location = 1) in vec3 PassVelocity[];
layout (location = 2) in vec3 PassAcceleration[];
layout (location = 3) in float PassSpriteSize[];
layout (location = 4) in float PassScale[];
layout (location = 5) in float PassAge[];
layout (location = 6) in float PassLifetime[];
layout (location = 7) in vec4 PassColor[];
layout (location = 8) in vec4 PassOrientation[];
layout (location = 9) in vec3 PassAngularVelocity[];

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

void EmitParticle() {
    OutPosition = PassPosition[0];
    OutVelocity = PassVelocity[0];
    OutAcceleration = PassAcceleration[0];
    OutSpriteSize = PassSpriteSize[0];
    OutScale = PassScale[0];
    OutAge = PassAge[0];
    OutLifetime = PassLifetime[0];
    OutColor = PassColor[0];
    OutOrientation = PassOrientation[0];
    OutAngularVelocity = PassAngularVelocity[0];

    EmitVertex();
    EndPrimitive();
}

void main() {
    EmitParticle();
}