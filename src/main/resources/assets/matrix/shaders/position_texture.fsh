#version 410 core

layout (location = 0) in vec3 Position;
layout (location = 1) in vec2 UV0;

uniform mat4 modelViewMatrix = mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
uniform mat4 projectionMatrix = mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);

layout (location = 0) out vec2 fragTexCoord;

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(Position, 1.0);
    fragTexCoord = UV0;
}
