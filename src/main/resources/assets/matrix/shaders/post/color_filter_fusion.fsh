#version 330 core

in vec2 fragTexCoord;

uniform sampler2D primaryFramebuffer;
uniform sampler2D secondaryFramebuffer;

out vec4 fragColor;

void main() {
    vec4 primaryFramebufferColor = texture(primaryFramebuffer, fragTexCoord);
    vec4 secondaryFramebufferColor = texture(secondaryFramebuffer, fragTexCoord);
    vec3 rgbColor = vec3(1) - (1 - primaryFramebufferColor.rgb) * (1 - secondaryFramebufferColor.rgb);
    float alpha = (primaryFramebufferColor.a + secondaryFramebufferColor.a) / 2;
    fragColor = vec4(1) - (1 - primaryFramebufferColor) * (1 - secondaryFramebufferColor);
    fragColor.a = alpha;
}