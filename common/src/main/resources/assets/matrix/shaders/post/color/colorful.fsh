#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;
uniform float brightness = 1.0;
uniform float saturation = 1.0;
uniform float contrast = 1.0;

out vec4 fragColor;

const vec3 luminanceCoefficients = vec3(0.2125, 0.7154, 0.0721);

void main() {
    vec4 color = texture(framebuffer, fragTexCoord);
    vec3 brightnessColor = color.rgb * brightness;
    float intensity = dot(brightnessColor, luminanceCoefficients);
    vec3 intensityColor = vec3(intensity);

    vec3 saturationColor = mix(intensityColor, brightnessColor, saturation);
    vec3 contrastColor = mix(color.rgb, saturationColor, contrast);
    fragColor = vec4(contrastColor, 1.0);
}