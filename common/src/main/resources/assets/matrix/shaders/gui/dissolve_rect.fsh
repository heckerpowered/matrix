#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 dissolveParams;

out vec4 fragColor;

const float emissiveStrength = 15.0;
const float pixelStrength = 16.0;
const float detialStrength = 1.0;
const vec4 emissiveColor = vec4(0.1, 0.5, 1.0, 1.0);

vec2 dissolveTexCoord() {
    vec2 uv = texCoord0 - 0.5;
    // dissolveParams.y carries (height/width) / 4 packed into the Normal channel.
    uv.y *= max(dissolveParams.y * 4.0, 0.0001);
    return uv + 0.5;
}

float pixelColor() {
    vec2 pixelTexCoord = dissolveTexCoord();
    float time = dissolveParams.z * 10.0;
    return texture(Sampler0, pixelTexCoord + vec2(time * 0.1, time * 0.1)).b;
}

float pixelAnimation() {
    float pixelNoise = dissolveTexCoord().r;
    return pixelNoise - mix(1.5, -1.5, dissolveParams.x);
}

float border() {
    vec4 normalColor = texture(Sampler0, ceil(dissolveTexCoord() * pixelStrength) / pixelStrength);
    vec4 offsetColor = texture(Sampler0, ceil((dissolveTexCoord() + 0.01) * pixelStrength) / pixelStrength);
    return (normalColor.b - offsetColor.b) * detialStrength;
}

void main() {
    float opacityMask = clamp((pixelColor() + border()) - pixelAnimation(), 0.0, 1.0);

    vec4 color = vertexColor;
    color.a *= ceil(opacityMask);
    color.rgb = pow(1.0 - opacityMask, 10.0) * (emissiveColor.rgb * emissiveStrength);
    fragColor = color * ColorModulator;
}
