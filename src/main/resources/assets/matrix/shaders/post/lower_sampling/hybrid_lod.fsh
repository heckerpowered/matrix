#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float primaryLevelOfDetail;
uniform float secondaryLevelOfDetail;
uniform float alpha = 0.5;

out vec4 fragColor;

void main() {
    vec4 primaryColor = textureLod(framebuffer, fragTexCoord, primaryLevelOfDetail);
    vec4 secondaryColor = textureLod(framebuffer, fragTexCoord, secondaryLevelOfDetail);
    vec4 mixColor = mix(primaryColor, secondaryColor, alpha);
    fragColor = mixColor;
}