#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform vec2 offset = vec2(1.0);

out vec4 fragColor;

void main() {
    vec2 texelSize = 1.0 / textureSize(framebuffer, 0);
    vec2 sampleOffset = offset * texelSize;
    vec4 color = texture(framebuffer, fragTexCoord);
    color += texture(framebuffer, fragTexCoord + vec2(sampleOffset.x, sampleOffset.y));
    color += texture(framebuffer, fragTexCoord + vec2(-sampleOffset.x, sampleOffset.y));
    color += texture(framebuffer, fragTexCoord + vec2(sampleOffset.x, -sampleOffset.y));
    color += texture(framebuffer, fragTexCoord + vec2(-sampleOffset.x, -sampleOffset.y));
    fragColor = color / 5;
}