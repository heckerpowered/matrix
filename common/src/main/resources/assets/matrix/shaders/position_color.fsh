#version 410 core

layout (location = 2) in vec4 vertexColor;

uniform vec4 colorModulator = vec4(1.0, 1.0, 1.0, 1.0);

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * colorModulator;
}
