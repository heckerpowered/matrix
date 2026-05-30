#version 330 core

in vec2 fragTexCoord;

uniform sampler2D primaryFramebuffer;
uniform sampler2D secondaryFramebuffer;

out vec4 fragColor;

void main() {
    vec4 primaryColor = texture(primaryFramebuffer, fragTexCoord);
    vec4 secondaryColor = texture(secondaryFramebuffer, fragTexCoord);

    vec3 blendedColor = 1.0 - (1.0 - primaryColor.rgb) * (1.0 - secondaryColor.rgb);
    float blendedAlpha = (primaryColor.a + secondaryColor.a) / 2;
    fragColor = vec4(blendedColor, blendedAlpha);
}