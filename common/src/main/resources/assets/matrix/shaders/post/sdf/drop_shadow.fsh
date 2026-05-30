#version 330 core

in vec2 fragTexCoord;

uniform sampler2D signedDistanceField;

uniform vec2 shadowOffset = vec2(0.0, 0.0);
uniform float shadowSize = 8.0;
uniform vec4 shadowColor = vec4(0.0, 0.0, 0.0, 0.5);

out vec4 fragColor;

void main() {
    vec2 resolution = textureSize(signedDistanceField, 0);
    float maxDistance = length(resolution);

    vec2 offsetCoord = fragTexCoord + shadowOffset / resolution;
    float dist = texture(signedDistanceField, offsetCoord).r;
    float denormalizedDist = dist * maxDistance;
    if (denormalizedDist < 0 || denormalizedDist > shadowSize) {
        discard;
    }

    float alpha = smoothstep(shadowSize, 0.0, denormalizedDist);
    fragColor = vec4(shadowColor.rgb, shadowColor.a * alpha);
}