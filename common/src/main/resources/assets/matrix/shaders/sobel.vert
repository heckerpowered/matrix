#version 410 core

layout (location = 0) in vec4 Position;
layout (location = 1) in vec2 UV0;

out vec2 fragTexCoord;

void main() {
    gl_Position = vec4(Position.xyz, 1.0);
    fragTexCoord = UV0;
}