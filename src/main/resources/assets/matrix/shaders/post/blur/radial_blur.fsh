#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float strength = 1.0;
uniform int samples = 10;

out vec4 fragColor;

const vec2 center = vec2(0.5, 0.5);

void main() {
    vec4 color = vec4(0);

    vec2 direction = (center - fragTexCoord) * strength * 0.01;
    for (int i = 0; i < samples; ++i) {
        color += texture(framebuffer, fragTexCoord + direction * float(i)) / float(samples);
    }

    fragColor = color;
}