#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D framebuffer;
uniform float lod = .0;
uniform sampler2D depthAttachment;

void main() {
    fragColor = textureLod(framebuffer, fragTexCoord, lod);
    gl_FragDepth = texture(depthAttachment, fragTexCoord).r;
    if (fragColor.r == 0.0 && fragColor.g == 0.0 && fragColor.b == 0.0) {
        discard;
    }
}