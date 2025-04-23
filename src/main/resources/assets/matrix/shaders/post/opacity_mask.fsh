#version 330 core

in vec2 fragTexCoord;

uniform sampler2D opacityMask;
uniform sampler2D colorAttachment;

out vec4 fragColor;

void main() {
    vec4 opacity = texture(opacityMask, fragTexCoord);
    if (opacity.a == .0) {
        discard;
    }
    fragColor = texture(colorAttachment, fragTexCoord);
}