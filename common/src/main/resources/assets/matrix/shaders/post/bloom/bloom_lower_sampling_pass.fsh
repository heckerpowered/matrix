#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform vec2 direction;

out vec4 fragColor;

const float weights[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec2 tex_offset = 1.0 / textureSize(framebuffer, 0);
    vec3 result = texture(framebuffer, fragTexCoord).rgb * weights[0];

    for (int i = 1; i < 5; ++i) {
        float weight = weights[i];
        vec2 offset = tex_offset * direction * i;
        result += texture(framebuffer, fragTexCoord + offset).rgb * weight;
        result += texture(framebuffer, fragTexCoord - offset).rgb * weight;
    }

    fragColor = vec4(result, 1.0);
}