#version 330 core

in vec4 previousClipPosition;
in vec4 currentClipPosition;

out vec4 fragColor;

void main() {
    vec2 currentNDC = currentClipPosition.xy / currentClipPosition.w;
    vec2 previousNDC = previousClipPosition.xy / previousClipPosition.w;
    vec2 velocity = currentNDC - previousNDC;
    fragColor = vec4(velocity, 0.0, 1.0);
}