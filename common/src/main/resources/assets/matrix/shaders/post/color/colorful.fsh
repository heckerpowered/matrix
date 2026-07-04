#version 330 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define brightness MatrixPostData0.x
#define saturation MatrixPostData0.y
#define contrast MatrixPostData0.z

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
