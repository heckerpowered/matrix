#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform vec4 color = vec4(1.0, 1.0, 1.0, 1.0);

out vec4 fragColor;

void main() {
    vec4 framebufferColor = texture(framebuffer, fragTexCoord);
    fragColor = framebufferColor * color;
}