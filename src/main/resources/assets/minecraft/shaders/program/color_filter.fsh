#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

uniform float RedFilter;
uniform float GreenFilter;
uniform float BlueFilter;

void main() {
    vec3 OutColor = vec3(fragColor.r * RedFilter, fragColor.g * GreenFilter, fragColor.b * BlueFilter);
    DiffuseSampler;
    // fragColor = vec4(OutColor, 1.0);
    // fragColor = vec4(0, 0, 0, 0);
}