#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float stepSize;

out vec4 fragColor;

void main() {
    vec2 bestPos = texture(framebuffer, fragTexCoord).xy;
    float bestDist = 1e9;

    vec2 resolution = textureSize(framebuffer, 0);
    for (int dx = -1; dx <= 1; ++dx)
    for (int dy = -1; dy <= 1; ++dy) {
        vec2 offset = vec2(dx, dy) * stepSize / resolution;
        vec2 candidate = texture(framebuffer, fragTexCoord + offset).xy;

        if (candidate.x >= 0.0) {
            float dist = length((fragTexCoord - candidate) * resolution);
            if (dist < bestDist) {
                bestDist = dist;
                bestPos = candidate;
            }
        }
    }

    fragColor = vec4(bestPos, 0.0, 1.0);
}