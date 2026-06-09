#version 410 core

in vec2 fragTexCoord;

uniform sampler2D framebuffer;

layout(std140) uniform MatrixPostUniforms {
    mat4 modelViewMatrix;
    mat4 projectionMatrix;
    vec4 circleParams0;
    vec4 circleParams1;
};

#define center circleParams0.xyz
#define radius circleParams0.w
#define resolution circleParams1.xy
#define grayscaleIntensity circleParams1.z

out vec4 fragColor;

void main() {
    vec4 framebufferColor = texture(framebuffer, fragTexCoord);
    vec4 clipPosition = projectionMatrix * modelViewMatrix * vec4(center, 1.0);

    // 剔除位于视锥体外的点
    if (clipPosition.w <= 0.0) {
        fragColor = framebufferColor;
        return;
    }

    // 计算屏幕坐标（修正Y轴方向）
    vec3 ndc = clipPosition.xyz / clipPosition.w;
    vec2 screenPosition = (ndc.xy * 0.5 + 0.5) * resolution;
    screenPosition.y = resolution.y - screenPosition.y; // 翻转Y轴

    // 转换纹理坐标到像素空间
    vec2 fragScreenPos = fragTexCoord * resolution;

    // 计算距离并绘制圆形
    float dist = length(fragScreenPos - screenPosition);
    if (dist > radius) {
        fragColor = framebufferColor;
    } else {
        float grayscale = dot(framebufferColor.rgb, vec3(0.299, 0.587, 0.114));
        vec3 color = mix(framebufferColor.rgb, vec3(grayscale), grayscaleIntensity);
        fragColor = vec4(color, framebufferColor.a);
    }
}
