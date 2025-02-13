#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D image;
uniform float opacity = 0;

void main() {
    fragColor = texture(image, fragTexCoord);
    if (opacity < 0.1) {
        discard;
    }
}