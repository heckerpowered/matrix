#version 330 core

in vec2 fragTexCoord;

uniform sampler2D depthTexture;
uniform sampler2D objectDepthTexture;
uniform sampler2D objectTexture;
uniform vec4 auraColor = vec4(0, 0, 0, 0);

out vec4 fragColor;

void main() {
    float sceneDepth = texture(depthTexture, fragTexCoord).r;
    float objectDepth = texture(objectDepthTexture, fragTexCoord).r;

    vec4 objectColor = texture(objectTexture, fragTexCoord);
    if (objectColor.a <= .0 || (objectColor.r <= 0 && objectColor.g <= 0 && objectColor.b <= 0)) {
        discard;
    }

    fragColor = objectColor;
    if (objectDepth >= sceneDepth) {
        fragColor = auraColor;
    }
}