#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

out vec4 fragColor;

void main() {
    fragColor = texture(framebuffer, fragTexCoord);
}
