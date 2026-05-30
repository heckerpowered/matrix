#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float levelOfDetail;

out vec4 fragColor;

void main() {
    fragColor = textureLod(framebuffer, fragTexCoord, levelOfDetail);
}