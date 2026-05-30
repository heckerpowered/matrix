#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float grayscaleIntensity = 0.0;

out vec4 fragColor;

void main() {
    vec4 framebufferColor = texture(framebuffer, fragTexCoord);
    float grayscale = dot(framebufferColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 color = mix(framebufferColor.rgb, vec3(grayscale), grayscaleIntensity);
    fragColor = vec4(color, framebufferColor.a);
}