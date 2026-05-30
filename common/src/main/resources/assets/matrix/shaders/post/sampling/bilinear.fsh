#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform vec2 sourceResolution;
uniform vec2 targetResolution;

out vec4 fragColor;

void main() {
    vec2 resolutionScale = sourceResolution / targetResolution;
    vec2 pixelSize = 1.0 / targetResolution / resolutionScale;
    vec2 sourceTexCoord = fragTexCoord;

    vec4 color = texture(framebuffer, sourceTexCoord);
    color += texture(framebuffer, sourceTexCoord + vec2(pixelSize.x, 0));
    color += texture(framebuffer, sourceTexCoord + vec2(0, pixelSize.y));
    color += texture(framebuffer, sourceTexCoord + pixelSize);
    color /= 4;

    fragColor = color;
}