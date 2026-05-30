#version 410 core

layout (location = 0) in vec4 Position;

uniform mat4 previousModelViewProjectionMatrix;
uniform mat4 currentModelViewProjectionMatrix;

out vec4 previousClipPosition;
out vec4 currentClipPosition;

void main() {
    currentClipPosition = currentModelViewProjectionMatrix * vec4(Position.xyz, 1.0);
    previousClipPosition = previousModelViewProjectionMatrix * vec4(Position.xyz, 1.0);
    gl_Position = currentClipPosition;
}