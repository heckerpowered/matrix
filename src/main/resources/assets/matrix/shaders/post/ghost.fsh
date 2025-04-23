#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float strength;

out vec4 fragColor;

void main() {
    vec2 centeredTexCoord = fragTexCoord - 0.5;
    vec2 edgeFactor = abs(centeredTexCoord) * 2;
    vec2 directionToEdge = normalize(centeredTexCoord);

    vec2 offset = directionToEdge * edgeFactor * strength * 0.05;

    float blurRadius = strength * length(centeredTexCoord) * 0.25;
    int samples = 10;

    vec4 blurColor = vec4(0.0);
    for (int i = -samples; i <= samples; i++) {
        float weight = 1.0 - abs(float(i)) / float(samples);
        vec2 sampleOffset = offset + directionToEdge * blurRadius * float(i) / float(samples);
        blurColor += texture(framebuffer, clamp(fragTexCoord + sampleOffset, 0.001, 0.999)) * weight;
    }
    blurColor /= float(2 * samples + 1);

    vec4 original = texture(framebuffer, fragTexCoord);

    float blendFactor = smoothstep(0.0, 1.0, length(centeredTexCoord) * strength * 2.0);

    fragColor = mix(original, blurColor, blendFactor);
}