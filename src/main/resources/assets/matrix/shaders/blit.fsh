#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D framebuffer;

void main() {
    fragColor = texture(framebuffer, fragTexCoord);
    if (fragColor.r == 0.0 && fragColor.g == 0.0 && fragColor.b == 0.0) {
        discard;
    }
}