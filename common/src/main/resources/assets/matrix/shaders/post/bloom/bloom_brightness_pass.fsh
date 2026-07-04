#version 150 core

uniform sampler2D framebuffer;// Input texture from the previous pass

layout(std140) uniform MatrixPostUniforms {
    vec4 MatrixPostData0;
    vec4 MatrixPostData1;
    vec4 MatrixPostData2;
    vec4 MatrixPostData3;
};

#define bloomThreshold MatrixPostData0.x
#define bloomIntensity MatrixPostData0.y

in vec2 fragTexCoord;// Texture coordinates from vertex shader

out vec4 fragColor;// Output fragment color

// Function to calculate perceived brightness (luminance)
float calculate_brightness(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

vec4 adjust_to_target_brightness(vec4 color, float targetBrightness) {
    float currentBrightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    float scale = targetBrightness / max(currentBrightness, 1e-5);
    return color * scale;
}

vec3 extractBloomSoft(vec3 color, float threshold, float knee) {
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float softness = clamp((brightness - threshold) / knee, 0.0, 1.0);
    float delta = softness * softness * (3.0 - 2.0 * softness);// smoothstep
    float target = delta * (brightness - threshold);

    float scale = target / max(brightness, 1e-5);
    return color * scale;
}

void main() {
    // Sample the color from the input texture
    vec4 color = texture(framebuffer, fragTexCoord);

    // Calculate the brightness of the sampled color
    float brightness = calculate_brightness(color.rgb);

    // Determine the factor based on the threshold
    // step(edge, x) returns 0.0 if x < edge, and 1.0 if x >= edge
    float factor = step(bloomThreshold, brightness);

    // Output the final color:
    // If brightness is above threshold (factor = 1.0), keep the original color.
    // If brightness is below threshold (factor = 0.0), output black.
    if (brightness < bloomThreshold) {
        fragColor = vec4(0.0, 0.0, 0.0, 0.0);
    } else {
        fragColor = vec4(extractBloomSoft(color.rgb, bloomThreshold, 1.0), color.a);
    }
}
