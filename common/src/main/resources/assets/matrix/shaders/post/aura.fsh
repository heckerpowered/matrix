#version 330 core

in vec2 fragTexCoord;

uniform sampler2D entityDepthAttachment;
uniform sampler2D entityColorAttachment;
uniform sampler2D sceneDepthAttachment;
uniform sampler2D sceneColorAttachment;
uniform sampler2D noiseColorAttachment;

layout(std140) uniform MatrixPostUniforms {
    vec4 auraParams;
    vec4 auraColor;
};

#define time auraParams.x
#define alpha auraParams.y

out vec4 fragColor;

float calculate_brightness(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    float entityDepth = texture(entityDepthAttachment, fragTexCoord).r;
    vec4 entityColor = texture(entityColorAttachment, fragTexCoord);
    float sceneDepth = texture(sceneDepthAttachment, fragTexCoord).r;
    vec4 sceneColor = texture(sceneColorAttachment, fragTexCoord);

    if (entityColor.a <= 0.0 || (entityColor.r <= 0 && entityColor.g <= 0 && entityColor.b <= 0)) {
        fragColor = sceneColor;
        return;
    }

    if (entityDepth >= sceneDepth) {
        vec4 noiseColor = texture(noiseColorAttachment, fragTexCoord + time);
        vec4 color = (auraColor + calculate_brightness(entityColor.rgb) + noiseColor) / 3;
        fragColor = mix(sceneColor, color, alpha);
    } else {
        fragColor = entityColor;
    }
}
