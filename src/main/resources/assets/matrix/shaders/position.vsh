#version 410 core

layout (location = 0) in vec3 Position;

uniform mat4 modelViewMatrix = mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);
uniform mat4 projectionMatrix = mat4(1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0);

out vec4 VertexPosition;

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(Position, 1.0);
    VertexPosition = gl_Position;
}