#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float kernel[49];
uniform int kernelSize = 48;
uniform vec2 direction = vec2(1, 0);

out vec4 fragColor;

void main() {
    vec4 originalColor = texture(framebuffer, fragTexCoord);
    if (kernelSize == 0) {
        fragColor = originalColor;
        return;
    }
    vec4 color = originalColor * kernel[0];
    vec2 offset = direction / vec2(textureSize(framebuffer, 0));

    for (int i = 1; i <= kernelSize; ++i) {
        vec2 subOffset = offset * float(i);
        color += texture(framebuffer, fragTexCoord + subOffset) * kernel[i];
        color += texture(framebuffer, fragTexCoord - subOffset) * kernel[i];
    }

    fragColor = color;
}