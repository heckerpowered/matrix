// Template for reconstructing world position from depth buffer

#version 330 core

in vec2 fragTexCoord;

uniform sampler2D depthAttachment;
uniform mat4 inverseModelViewMatrix;
uniform mat4 inverseProjectionMatrix;
uniform vec2 resolution;

out vec4 fragColor;

void main() {
    // Map screen space position to NDC
    vec2 ndc = (fragTexCoord / resolution) * 2.0 - 1.0;
    ncd.y = -ndc.y;

    float depth = texture(depthAttachment, fragTexCoord).r;
    vec4 clipPosition = vec4(ndc, depth * 2.0 - 1.0, 1.0);
    vec4 viewPosition = inverseProjectionMatrix * clipPosition;
    viewPosition /= viewPosition.w;// Perspective divide
    vec4 worldPosition = inverseModelViewMatrix * viewPosition;
    worldPosition /= worldPosition.z;
}