#version 330

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform sampler2D depthAttachment;

layout(std140) uniform BlitConfig {
    float lod;
};

out vec4 fragColor;

void main() {
    fragColor = textureLod(framebuffer, fragTexCoord, lod);
    gl_FragDepth = texture(depthAttachment, fragTexCoord).r;
    if (fragColor.r == 0.0 && fragColor.g == 0.0 && fragColor.b == 0.0) {
        discard;
    }
}
