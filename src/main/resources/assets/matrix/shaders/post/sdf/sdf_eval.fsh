#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform sampler2D originFramebuffer;

out vec4 fragColor;

void main() {
    vec2 resolution = textureSize(framebuffer, 0);
    vec2 nearest = texture(framebuffer, fragTexCoord).xy;
    float maxDistance = length(resolution);
    float dist = length((fragTexCoord - nearest) * resolution) / maxDistance;
    vec4 color = texture(originFramebuffer, fragTexCoord);
    if (color.a != .0) {
        dist = -dist;
    }

    fragColor = vec4(dist, dist, dist, 1.0);
}