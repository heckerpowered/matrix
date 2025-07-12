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

uniform mat4 ProjectionMatrix = mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
uniform mat4 ModelViewMatrix = mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);

out vec4 Color;

void main()
{
    gl_Position = ProjectionMatrix * ModelViewMatrix * vec4(InPosition, 1.0);
    float distance = length((ModelViewMatrix * vec4(InPosition, 1.0)).xyz);
    gl_PointSize = InSpriteSize * InScale / distance;

    Color = InColor;
}