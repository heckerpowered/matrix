#version 330 core

in vec2 fragTexCoord;

uniform float progress = 1.0;
uniform float radius = 0.5;
uniform float thickness = 0.1;
uniform vec2 center = vec2(0.5, 0.5);
uniform vec4 color = vec4(1.0);

out vec4 fragColor;

#define PI 3.141592653589793

void main() {
    vec2 direction = fragTexCoord - center;
    float distance = length(direction);
    if (distance < radius - thickness || distance > radius) {
        discard;
    }

    float angle = atan(direction.y, direction.x);
    if (angle < 0.0) {
        // Map angle from (-π, π] to [0, 2π)
        angle += 2.0 * PI;
    }

    // Ring starts at +Y axis (top)
    float start = 0.5 * PI;
    float sweep = progress * 2.0 * PI;
    float end = start - sweep;

    bool inRange;
    if (end < .0) {
        inRange = angle <= start && angle >= 0.0 || angle >= end + 2.0 * PI && angle <= 2.0 * PI;
    } else {
        inRange = angle <= start && angle >= end;
    }

    if (!inRange) {
        discard;
    }
    fragColor = color;
}