#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float lod = .0;

out vec4 fragColor;

void main() {
    fragColor = textureLod(framebuffer, fragTexCoord, lod);
    if (fragColor.r == 0.0 && fragColor.g == 0.0 && fragColor.b == 0.0) {
        discard;
    }
}