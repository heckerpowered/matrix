#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

out vec4 fragColor;

void main() {
    vec4 color = texture(framebuffer, fragTexCoord);
    if (color.a != .0) {
        fragColor = vec4(fragTexCoord, 0.0, 1.0);
    } else {
        fragColor = vec4(-1.0, -1.0, 0.0, 0.0);
    }
}