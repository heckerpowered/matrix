#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D framebuffer;
uniform float weight[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
uniform float radius = 5.0;

void main() {
    vec2 tex_offset = 1.0 / textureSize(framebuffer, 0) * radius;
    vec3 result = texture(framebuffer, fragTexCoord).rgb * weight[0];

    for (int i = 1; i < 5; ++i) {
        result += texture(framebuffer, fragTexCoord + vec2(0.0, tex_offset.y * i)).rgb * weight[i];
        result += texture(framebuffer, fragTexCoord - vec2(0.0, tex_offset.y * i)).rgb * weight[i];
    }

    fragColor = vec4(result, 1.0);
}