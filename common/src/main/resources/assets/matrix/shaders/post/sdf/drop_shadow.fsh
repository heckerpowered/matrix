#version 330 core

in vec2 fragTexCoord;

uniform sampler2D signedDistanceField;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define shadowOffset MatrixPostData0.xy
#define shadowSize MatrixPostData0.z
#define shadowColor MatrixPostData1

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
