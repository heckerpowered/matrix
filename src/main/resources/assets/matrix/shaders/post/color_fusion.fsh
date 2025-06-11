#version 330 core

in vec2 fragTexCoord;

uniform sampler2D primaryFramebuffer;
uniform sampler2D secondaryFramebuffer;

uniform vec4 colorMultiplier = vec4(1.0, 1.0, 1.0, 1.0);

out vec4 fragColor;

void main() {
    vec4 primaryFramebufferColor = texture(primaryFramebuffer, fragTexCoord);
    vec4 secondaryFramebufferColor = texture(secondaryFramebuffer, fragTexCoord);
    vec3 rgbColor = primaryFramebufferColor.rgb + secondaryFramebufferColor.rgb;
    float alpha = (primaryFramebufferColor.a + secondaryFramebufferColor.a) / 2;
    fragColor = (primaryFramebufferColor + secondaryFramebufferColor) * colorMultiplier;
    // fragColor.a = alpha;
}